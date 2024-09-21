

# JWT   2024/09/03

## 线程容器

新增管理用户id的线程容器，方便其他业务操作获取用户id。注意，不应创建为静态类

```java
package com.blog.authentication;

import org.springframework.stereotype.Component;

@Component
public class CurrentUserHolder {
    private ThreadLocal<Long> currentUser = new ThreadLocal<>();

    public void setUserId(Long userId) {
        currentUser.set(userId);
    }

    public Long getUserId() {
        return currentUser.get();
    }

    public void clear() {
        currentUser.remove();
    }
}
```

## 过滤器

并在过滤器中利用其存储用户id，方便业务操作调用用户id。注意，不要在方法内部去`new`什么东西，考虑下垃圾回收的问题。学习下`netty`源码中的对象池管理对象方法。


```java
package com.blog.authentication;

import com.blog.enums.ErrorCode;
import com.blog.exception.BusinessException;
import com.blog.util.JwtProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthenticationFilter implements Filter {

    private final JwtProcessor jwtProcessor;
    private final CurrentUserHolder currentUserHolder;
    private final Set<String> ALLOWED_PATHS = new HashSet<>(Arrays.asList(
            "/users/login",
            "/users/refresh",
            "/users/email_code",
            "/users/register",
            "/users/logout"
    ));

    @Override
    public void doFilter(@NotNull ServletRequest servletRequest, @NotNull ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String accessToken = request.getHeader("accessToken");

        String requestURI = request.getRequestURI();
        /*登录、注册、令牌刷新等操作不验证身份，对其他的业务操作进行验证*/

        try {
            if (ALLOWED_PATHS.contains(requestURI)) {
                filterChain.doFilter(request, response);
                return;
            }
            //extractUserId内部进行token验证时会对invalidToken的异常进行处理
            Map<String, Object> userMap = jwtProcessor.extractUserMap(accessToken);
            Long userId = (Long) userMap.get("userId");
            currentUserHolder.setUserId(userId);
            filterChain.doFilter(request, response);
        } finally {
            //注意：当前请求结束后一定要清理线程，不然会有内存泄漏，
            // 下一个请求会服用上一个请求用户Id等风险
            currentUserHolder.clear();
        }

    }
}
```

```java
package com.blog.util;

import com.blog.enums.ErrorCode;
import com.blog.exception.BusinessException;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@Slf4j
public class JwtProcessor {
    @Value("${security.jwt.secret}")
    private String secretKey;

    @Value("${security.jwt.expiration}")
    private int jwtExpiration;

    /**
     * Token令牌验证
     *
     * @param token
     * @param userId
     * @return Boolean
     */
    public Boolean validateToken(String token, Long userId) {
        final Map<String, Object> userMap = extractUserMap(token);
        return userMap.get("userId").equals(userId);
    }


    /**
     * 生成Token令牌
     *
     * @param userMap
     * @return String
     */
    public String generateToken(Map<String, Object> userMap) {
        return createToken(userMap, jwtExpiration);
    }

    public Map<String, Object> extractUserMap(String token) {
        Map<String, Object> map = extractAllClaims(token);
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("userId", map.get("userId"));
        userMap.put("nickName", map.get("nickName"));
        userMap.put("account", map.get("account"));
        return userMap;
    }

    /**
     * 刷新令牌
     *
     * @param userMap
     * @return String
     */

    public String generateRefreshToken(Map<String, Object> userMap) {
        return createToken(userMap, jwtExpiration * 4 * 24 * 7);
    }
    
    public Claims extractAllClaims(String token) {
        Claims claims;
        try {
            claims =  Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();
        }catch (MalformedJwtException|UnsupportedJwtException|IllegalArgumentException e){
            log.error("非法的令牌格式");
            throw new BusinessException(ErrorCode.TOKEN_ERROR,"非法的令牌格式");
        }catch (ExpiredJwtException e){
            log.error("令牌已过期");
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED,"令牌已过期");
        }
        return claims;
    }

    private String createToken(Map<String, Object> claims, int expiration) {
        final Date date = DateUtils.addMinutes(new Date(),expiration);
        return Jwts.builder().setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(date)
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }

}

```

<u>创建`token`的参数为`userMap`，包含了`userID`、`nickName`、`account`等非敏感信息。将该**错误转译**放置在底层方法`extractAllClaims`中，其中，**`MalformedJwtException`**是指`JWT`格式不正确，传入的`token`为人造非有效的字符串时会抛出的异常。`**UnsupportedJwtException**`是指当前传入的`token`使用了不正确的签名算法时抛出的异常。`**IllegalArgumentException**`为方法传入参数异常，指当前传入的token为空或者非字符串类型时会抛出的异常。</u>

因过滤器中并未对`token`中的`id`与当前线程`id`是否一直进行校验。以防恶意使用他人非法`token`绕过过滤器校验，在其他层业务操作前先进行`id`验证，如下：

```java
    if (!jwtProcessor.validateToken(accessToken, userId)) {
        return ResponseEntity.ok("token验证失败");
    }
```



## RefreshToken 09/04

`UserService.refreshAccessToken`方法中新增了对刷新令牌的验证，以防恶意利用其他用户`refreshToken`刷新

```java
    //对刷新令牌进行验证，以防恶意利用其他用户refreshToken刷新
    if (!jwtProcessor.validateToken(refreshToken, userId)) {
        log.error("refreshToken验证失败");
        throw new BusinessException(ErrorCode.TOKEN_ERROR, "refreshToken验证失败");
    }
```

`UserController.refreshToken`方法中新增了对当前用户状态是否异常的判断，若为异常用户，则无法刷新`token`。`userId`应当从`redis`中获取

```java

    /**
     * 刷新token
     *
     * @param refreshToken
     * @return ResponseResult
     * @author zhang
     */
    @PostMapping("/refresh")
    public ResponseResult<String> refreshToken(String refreshToken) {
        Map<String, Object> userMap = jwtProcessor.extractUserMap(refreshToken);
        Long userId = (Long) userMap.get("userId");
        User user = userMapper.selectByPrimaryKey(userId);
        if (user.getStatus() == 0) {
            log.error("该用户处于异常状态，无法执行下一步操作");
            throw new BusinessException("该用户处于异常状态，无法执行下一步操作");
        }
        //返回给前端刷新后的accessToken,同时也会产生新的refreshToken,以防refreshToken过期
        String accessToken = userService.refreshAccessToken(refreshToken, userId);
        return ResponseResult.success(accessToken);
    }
```



## JwtProcessor内部异常处理

注意`extractAllClaims`方法中的验证异常处理

```java
package com.blog.util;

import com.blog.enums.ErrorCode;
import com.blog.exception.BusinessException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class JwtProcessor {
    @Value("${security.jwt.secret}")
    private String secretKey;

    @Value("${security.jwt.expiration}")
    private int jwtExpiration;

    /**
     * Token令牌验证
     *
     * @param token
     * @param userId
     * @return Boolean
     */
    public Boolean validateToken(String token, Long userId) {
        final Map<String, Object> userMap = extractUserMap(token);
        return userMap.get("id").equals(userId);
    }


    /**
     * 生成Token令牌
     *
     * @param userMap
     * @return String
     */
    public String generateToken(Map<String, Object> userMap) {
        return createToken(userMap, jwtExpiration);
    }

    public Map<String, Object> extractUserMap(String token) {
        Map<String, Object> map = extractAllClaims(token);
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("userId", map.get("userId"));
        userMap.put("nickName", map.get("nickName"));
        userMap.put("account", map.get("account"));
        return userMap;
    }

    /**
     * 刷新令牌
     *
     * @param userMap
     * @return String
     */

    public String generateRefreshToken(Map<String, Object> userMap) {
        return createToken(userMap, jwtExpiration * 4 * 24 * 7);
    }

    private Claims extractAllClaims(String token) {
        Claims claims;
        try {
            claims =  Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();
        }catch (MalformedJwtException | UnsupportedJwtException | IllegalArgumentException | SignatureException e){
            log.error("非法的令牌格式");
            throw new BusinessException(ErrorCode.TOKEN_ERROR,"非法的令牌格式");
        }catch (ExpiredJwtException e){
            log.error("令牌已过期");
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED,"令牌已过期");
        }
        return claims;
    }

    private String createToken(Map<String, Object> claims, int expiration) {
        final Date date = DateUtils.addMinutes(new Date(),expiration);
        return Jwts.builder().setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(date)
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
    }

}

```

总结：目前token校验使用没发现问题，再了解下是否还有其他需要注意的地方。

## 线程相关

### Thread和ThreadLocal

在Java中，`Thread` 和 `ThreadLocal` 是两个与多线程编程密切相关的概念，它们用于处理并发执行的任务和线程局部变量。

#### Thread
`Thread` 是Java中实现多线程的一种基本机制。每个 `Thread` 对象代表一个线程的实例，它有自己独立的执行栈和局部变量。线程可以是用户线程或守护线程（Daemon thread）。用户线程是默认的线程类型，当所有用户线程结束时，程序也会结束。守护线程通常用于执行一些后台任务，如垃圾回收，当所有用户线程结束时，即使守护线程还在运行，程序也会结束。

线程的生命周期包括以下几个状态：
- **新建（New）**：线程对象已被创建，但还没有调用 `start()` 方法。
- **可运行（Runnable）**：线程已经调用了 `start()` 方法，但可能因为其他线程正在运行而没有获得CPU时间。
- **阻塞（Blocked）**：线程正在等待监视器锁（synchronized block/statement）以进入同步区域。
- **等待（Waiting）**：线程通过调用 `wait()`, `join()`, `LockSupport.park()` 等方法进入等待状态，需要其他线程的特定动作才能继续执行。
- **计时等待（Timed Waiting）**：类似于等待状态，但有一个最大等待时间，可以通过 `sleep(long millis)`, `wait(long timeout)`, `LockSupport.parkNanos()`, `LockSupport.parkUntil()` 等方法进入这个状态。
- **终止（Terminated）**：线程的运行结束，可能是因为 `run()` 方法执行完毕，或者因为某个未捕获的异常。

#### ThreadLocal
`ThreadLocal` 是Java提供的一种线程局部变量机制，它允许每个线程拥有自己的变量副本。这意味着，当多个线程访问同一个 `ThreadLocal` 变量时，它们实际上是访问各自独立的变量副本，从而避免了线程安全问题。

`ThreadLocal` 的主要方法包括：
- **set(T value)**：为当前线程设置一个局部变量的值。
- **get()**：获取当前线程所对应的局部变量的值。
- **remove()**：移除当前线程所对应的局部变量，以及其值。

`ThreadLocal` 的使用场景包括：
- 保存线程特定的数据，如用户会话信息、事务信息等。
- 避免在方法调用中传递参数，从而简化代码。

需要注意的是，`ThreadLocal` 变量如果不手动清除，可能会导致内存泄漏，特别是在使用线程池时。因为线程池中的线程通常会被重用，如果不清除 `ThreadLocal` 变量，那么线程可能会保留上一个任务的局部变量，这可能会导致不可预期的行为。因此，在使用完 `ThreadLocal` 变量后，应该调用 `remove()` 方法来清除。

内存泄漏可以理解为在每次执行GC后，内存会增加，直至GC清理不再起作用，整个瘫掉。

总结来说，`Thread` 是Java中实现线程的机制，而 `ThreadLocal` 是一种线程局部变量机制，用于在多线程环境中安全地存储线程特定的数据。

# BlogMapper 2024/09/03

## bean转换工具

可以转换`VO`对象和`entity`对象，很方便。可以使用`hutool`包中的`BeanUtils.copyProperties`方法。比如，在更新博客时只copy `bean`中的非null属性，这种情况可使用`hutool`中的`BeanUtil`工具，如下：

```java
BeanUtil.copyProperties(oldDetail, userDetail, 
    CopyOptions.create().setIgnoreNullValue(true).setIgnoreError(true));
```

## BlogService.  saveBlog+updateBlog

多使用功能函数编程。（多用多了解）

```java
package com.blog.service;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.blog.authentication.CurrentUserHolder;
import com.blog.constant.Constant;
import com.blog.entity.Blog;
import com.blog.entity.BlogTag;
import com.blog.entity.Category;
import com.blog.entity.Tag;
import com.blog.enums.ErrorCode;
import com.blog.exception.BusinessException;
import com.blog.mapper.BlogMapper;
import com.blog.mapper.BlogTagMapper;
import com.blog.mapper.CategoryMapper;
import com.blog.mapper.TagMapper;
import com.blog.util.SnowFlakeUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.util.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class BlogService {
    private final CategoryMapper categoryMapper;
    private final BlogMapper blogMapper;
    private final TagMapper tagMapper;
    private final BlogTagMapper blogTagMapper;

    private final CurrentUserHolder currentUserHolder;
    private final int MAX_TAG_COUNT = 6;


    /**
     * 新增博客，同时对博客的标签以及分类等关系进行处理
     *
     * @param blog
     */
    @Transactional
    public void saveBlog(Blog blog) {
        Integer categoryId = blog.getCategoryId();
        Category categoryExist = categoryMapper.selectByPrimaryKey(categoryId);
        Optional<Category> optionalCategory = Optional.ofNullable(categoryExist);
        Long userId = currentUserHolder.getUserId();
        Long blogId = SnowFlakeUtil.nextId();
        String baseHomePageUrl = String.format(Constant.BLOG_BASE_PATH + "%s/%s", userId, blogId);
        blog.setSubUrl(baseHomePageUrl);
        blog.setUserId(userId);
        blog.setBlogId(blogId);
        blogMapper.insertSelective(blog);
        handleCategoryAndBlog(blog, optionalCategory);
        handleTags(blog);
    }

    /**
     * 更新博客，同时对博客的标签以及分类等关系进行处理
     *
     * @param blog
     */
    @Transactional
    public void updateBlog(Blog blog) {
        Blog blogForUpdate = blogMapper.selectByPrimaryKey(blog.getBlogId());
        BeanUtil.copyProperties(blog, blogForUpdate, CopyOptions.create().setIgnoreNullValue(true).setIgnoreError(true));
        Category categoryExist = categoryMapper.selectByPrimaryKey(blogForUpdate.getCategoryId());
        Optional<Category> optionalCategory = Optional.ofNullable(categoryExist);
        blogMapper.updateByPrimaryKeySelective(blogForUpdate);
        handleCategoryAndBlog(blogForUpdate, optionalCategory);
        handleTags(blogForUpdate);
    }

    /**
     * 处理博客分类信息
     *
     * @param blog
     * @param categoryExist
     * @return Blog
     */
    private void handleCategoryAndBlog(Blog blog, Optional<Category> categoryExist) {
        /*如果分类是新增分类，需要输入categoryId和categoryName信息
         * 如果分类信息在分类表中存在，则对分类表rank进行更新*/
        if (!categoryExist.isPresent()) {
            Category category = new Category(blog.getCategoryId(), blog.getCategoryName());
            categoryMapper.insertSelective(category);
        } else {
            categoryMapper.increatCategoryRank(categoryExist.get());
        }
    }

    /**
     * 处理博客标签信息
     *
     * @param blog
     */

    private void handleTags(Blog blog) {
        if (StringUtil.isNotEmpty(blog.getBlogTags())) {
            String[] tags = blog.getBlogTags().split(",");
            if (tags.length > MAX_TAG_COUNT) {
                log.error("输入标签数量限制为{}，请重新输入", MAX_TAG_COUNT);
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "输入标签数量限制为{}，请重新输入", MAX_TAG_COUNT);
            }
            List<String> distinctTagNames = Arrays.stream(tags).distinct().collect(Collectors.toList());


            List<Tag> tagsFromDb = tagMapper.selectListByTagNames(distinctTagNames);
            List<Tag> mutableTagsFromDb = new ArrayList<>(tagsFromDb);
            //处理数据库中并不存在的标签，即需要新插入的标签
            List<Tag> tagListForInsert = distinctTagNames.stream()
                    .filter(tagName -> !mutableTagsFromDb.stream().map(Tag::getTagName).collect(Collectors.toSet()).contains(tagName))
                    .map(tagName -> new Tag(tagName))
                    .collect(Collectors.toList());

            if (!CollectionUtils.isEmpty(tagListForInsert)) {
                tagMapper.insertList(tagListForInsert);
                mutableTagsFromDb.addAll(tagListForInsert); // 将新插入的标签添加到数据库中
            }

            List<Tag> allTagsList = mutableTagsFromDb.stream()
                    .collect(Collectors.toList());

            List<BlogTag> blogTags = createBlogTags(blog, allTagsList);
            //删除当前博客已经关联的标签，再重新关联，适用于修改博客标签信息场景
            blogTagMapper.deleteByBlogId(blog.getBlogId());
            blogTagMapper.insertList(blogTags);


        }
    }

    /**
     * 处理博客标签关系，即博客与标签的关联表
     *
     * @param blog
     * @param tags
     * @return List<BlogTag>
     */
    private List<BlogTag> createBlogTags(Blog blog, List<Tag> tags) {
        List<BlogTag> blogTags = tags.stream()
                .map(tag -> new BlogTag(blog.getBlogId(), tag.getTagId()))
                .collect(Collectors.toList());
        return blogTags;
    }

    public List<Blog> getBlogList(Long userId, int pageNo, int pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<Blog> blogs = blogMapper.selectListByUserId(userId);
        return blogs;
    }

    public Optional<Blog> getBlogById(Long blogId) {
        return Optional.ofNullable(blogMapper.selectByPrimaryKey(blogId));
    }

    public List<Blog> getBlogListByCategoryId(Long categoryId, int pageNo, int pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<Blog> blogs = blogMapper.selectListByCategoryId(categoryId);
        return blogs;
    }

    public void deleteBlog(Long blogId) {
        blogMapper.deleteByPrimaryKey(blogId);
    }
}

```



## BlogController 

```java
package com.blog.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.blog.authentication.CurrentUserHolder;
import com.blog.dto.PageResult;
import com.blog.entity.Blog;
import com.blog.entity.User;

import com.blog.exception.BusinessException;
import com.blog.exception.ResponseResult;
import com.blog.service.BlogService;
import com.blog.service.UserService;
import com.blog.vo.blog.BlogDesc;
import com.blog.vo.blog.BlogDetail;
import com.blog.vo.blog.BlogUpdateVo;
import com.blog.vo.blog.BlogInVo;
import lombok.RequiredArgsConstructor;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/blogs")
@RequiredArgsConstructor
public class BlogController extends BaseController{
    private final CurrentUserHolder currentUserHolder;

    private final UserService userService;
    private final BlogService blogService;

    /**
     * 保存文章
     * @param blogInVo
     * @return ResponseResult
     * @author zhang
     */
    @PostMapping("/save")
    public ResponseResult<String> saveBlog(@RequestBody BlogVo blogInVo) {
        User user = userService.selectUserByUserId(blogInVo.getUserId())
                .orElseThrow(() -> new BusinessException("用户不存在！"));
        if (user.getStatus()==1){
            return ResponseResult.fail("该用户已被封禁，无法发布文章！");
        }
        Blog blog = new Blog();
        BeanUtil.copyProperties(blogInVo, blog);
        blogService.saveBlog(blog);
        return ResponseResult.success("文章保存成功");
    }

    /**
     * 根据博客id更新博客
     * @param blogUpdateVo
     * @param blogId
     * @return ResponseResult
     * @author zhang
     */
    @PutMapping("/update/{blogId}")
    public ResponseResult<String> updateBlog(@RequestBody BlogUpdateVo blogUpdateVo, @PathVariable Long blogId) {
        Blog blog = blogService.getBlogById(blogId)
                .orElseThrow(() -> new BusinessException("文章不存在！"));
        BeanUtil.copyProperties(blogUpdateVo, blog, CopyOptions.create().setIgnoreNullValue(true).setIgnoreCase(true));
        blogService.updateBlog(blog);
        return ResponseResult.success("文章更新成功");
    }

    /**
     * 获取当前用户的博客列表
     * @param pageNo
     * @param pageSize
     * @return ResponseResult
     * @author zhang
     */
    @GetMapping("/list")
    public ResponseResult<PageResult<BlogDesc>> getCurrentUserBlogList(@RequestParam int pageNo, @RequestParam int pageSize) {
        Long userId = currentUserHolder.getUserId();
        List<Blog> blogList = blogService.getBlogList(userId, pageNo, pageSize);
        if (CollectionUtils.isEmpty(blogList)) {
            return ResponseResult.fail("当前用户博客列表为空");
        }
        List<BlogDesc> blogDescList = blogList.stream()
                .map(blog -> BeanUtil.copyProperties(blog, BlogDesc.class))
                .collect(Collectors.toList());
        int totalCount = blogList.size();
        PageResult<BlogDesc> pageResult = new PageResult<>(blogDescList, totalCount);
        return ResponseResult.success(pageResult);
    }

    /**
     * 获取博客详情
     * @param blogId
     * @return ResponseResult
     * @author zhang
     */
    @GetMapping("/{blogId}")
    public ResponseResult<BlogDetail> getBlog(@PathVariable Long blogId) {
        Blog blog = blogService.getBlogById(blogId)
                .orElseThrow(() -> new BusinessException("文章不存在！"));
        BlogDetail blogDetail = new BlogDetail();
        BeanUtil.copyProperties(blog, blogDetail);
        return ResponseResult.success(blogDetail);
    }

    /**
     * 根据分类id获取博客列表
     * @param categoryId
     * @param pageNo
     * @param pageSize
     * @return ResponseResult
     * @author zhang
     */
    @GetMapping("/category/{categoryId}")
    public ResponseResult<List<BlogVo>> getBlogListByCategoryId(@PathVariable Long categoryId, @RequestParam int pageNo, @RequestParam int pageSize) {
        List<Blog> blogList = blogService.getBlogListByCategoryId(categoryId, pageNo, pageSize);
        if (CollectionUtils.isEmpty(blogList)) {
            return ResponseResult.fail("该分类下暂无文章");
        }
        List<BlogVo> blogInVoList = blogList.stream()
                .map(blog -> BeanUtil.copyProperties(blog, BlogVo.class))
                .collect(Collectors.toList());
        return ResponseResult.success(blogInVoList);
    }

    /**
     * 根据博客id删除博客
     * @param blogId
     * @return ResponseResult
     * @author zhang
     */
    @DeleteMapping("/delete/{blogId}")
    public ResponseResult<String> deleteBlog(@PathVariable Long blogId) {
        blogService.getBlogById(blogId)
                .orElseThrow(() -> new BusinessException("文章不存在！"));
        blogService.deleteBlog(blogId);
        return ResponseResult.success("删除成功");
    }
}

```



## BlogVo（雪花算法精度丢失）

在前后端交互时注意blogId精度丢失问题。即雪花算法生成的ID在前端会出现精度丢失。可使用JSON序列化转为String解决，方法如下：

```java
@JsonProperty("blogId")
@JsonFormat(shape = JsonFormat.Shape.STRING)
@JSONField(serializeUsing= ToStringSerializer.class)
private Long blogId;
```

# User

## UserController

```java
package com.blog.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.blog.authentication.CurrentUserHolder;
import com.blog.entity.User;

import com.blog.enums.ErrorCode;
import com.blog.exception.BusinessException;
import com.blog.exception.ResponseResult;
import com.blog.mapper.UserMapper;
import com.blog.service.MailService;
import com.blog.service.UserService;
import com.blog.util.JwtProcessor;
import com.blog.util.SecurityUtils;
import com.blog.util.UserTransUtils;
import com.blog.util.bo.LoginResponse;
import com.blog.util.redis.RedisProcessor;
import com.blog.util.redis.RedisTransKey;
import com.blog.vo.user.UserInfoVo;
import com.blog.vo.user.Loginer;
import com.blog.vo.user.Register;
import com.github.pagehelper.util.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Email;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@RestController
@RequestMapping("/users")
@Slf4j
@RequiredArgsConstructor
public class UserController extends BaseController {

    private final UserService userService;
    private final MailService mailService;
    private final RedisProcessor redisProcessor;
    private final UserMapper userMapper;
    private final JwtProcessor jwtProcessor;
    private final CurrentUserHolder currentUserHolder;

    /**
     * 用户注册
     *
     * @param register
     * @return ResponseResult
     * @author zhang
     */
    @PostMapping("/register")
    public ResponseResult<String> register(@RequestBody @Validated Register register) {
        userService.userRegister(register);
        return ResponseResult.success("用户注册成功");
    }

    /**
     * 用户获取邮箱验证码
     *
     * @param email
     * @return ResponseResult
     * @author zhang
     */
    @GetMapping("/getCode")
    @ResponseBody
    public ResponseResult<String> getCode(@RequestParam @Email String email) {
        mailService.getEmailCode(email);
        return ResponseResult.success("验证码发送成功");
    }

    /**
     * @param loginer
     * @return ResponseResult
     * @author zhang
     */
    @PostMapping("/login")
    @ResponseBody
    public ResponseResult<LoginResponse> login(@Validated @RequestBody Loginer loginer) {
        //用户登陆后返回给前端accessToken和refreshToken
        LoginResponse loginResponse = userService.userLogin(loginer);
        return ResponseResult.success(loginResponse);
    }

    /**
     * 刷新token
     *
     * @param refreshToken
     * @return ResponseResult
     * @author zhang
     */
    @PostMapping("/refresh")
    public ResponseResult<String> refreshToken(String refreshToken) {
        Map<String, Object> userMap = jwtProcessor.extractUserMap(refreshToken);
        Long userId = (Long) userMap.get("userId");
        User user = userMapper.selectByPrimaryKey(userId);
        if (user.getStatus() == 0) {
            log.error("该用户处于异常状态，无法执行下一步操作");
            throw new BusinessException("该用户处于异常状态，无法执行下一步操作");
        }
        //返回给前端刷新后的accessToken,同时也会产生新的refreshToken,以防refreshToken过期
        String accessToken = userService.refreshAccessToken(refreshToken, userId);
        return ResponseResult.success(accessToken);
    }

    /***
     * 用户退出登陆时，需要删除token信息
     * @param request
     * @return ResponseEntity
     * @Author zhang
     */
    @GetMapping("/logout")
    public ResponseResult<String> logout(HttpServletRequest request) {
        String accessToken = request.getHeader("accessToken");
        Long userId = currentUserHolder.getUserId();
        if (!jwtProcessor.validateToken(accessToken, userId)) {
            return ResponseResult.fail(ErrorCode.TOKEN_ERROR.getCode(), "token验证失败");
        }
        User user = userMapper.selectByPrimaryKey(userId);
        redisProcessor.del(RedisTransKey.getRefreshTokenKey(user.getEmail()));
        redisProcessor.del(RedisTransKey.getLoginKey(user.getEmail()));
        redisProcessor.del(RedisTransKey.getTokenKey(user.getEmail()));
        log.info("用户退出登陆成功");
        return ResponseResult.success("用户退出登陆成功");
    }

    /**
     * 用户删除登录会先对当前请求中的token进行验证
     *
     * @param request
     * @return ResponseResult
     * @author zhang
     */
    @DeleteMapping("/delete")
    public ResponseResult<String> delete(HttpServletRequest request) {
        String accessToken = request.getHeader("accessToken");
        Long userId = currentUserHolder.getUserId();
        userService.selectUserByUserId(userId)
                .orElseThrow(() -> new BusinessException("用户不存在！"));
        if (!jwtProcessor.validateToken(accessToken, userId)) {
            return ResponseResult.fail(ErrorCode.TOKEN_ERROR.getCode(), "token验证失败");
        }
        userService.deleteUserById(userId);
        return ResponseResult.success("用户删除成功");
    }

    /**
     * 用户更新信息前先对当前请求中的token进行验证
     *
     * @param userInfoVo
     * @return ResponseResult
     * @author zhang
     */
    @PutMapping("/update")
    public ResponseResult<String> updateUser(@RequestBody UserInfoVo userInfoVo) {
        String accessToken = (String) redisProcessor.get(RedisTransKey.getTokenKey(userInfoVo.getEmail()));
        Long userId = currentUserHolder.getUserId();
        if (!jwtProcessor.validateToken(accessToken, userId)) {
            return ResponseResult.fail(ErrorCode.TOKEN_ERROR.getCode(), "token验证失败");
        }
        User user = userService.selectUserByUserId(userId)
                .orElseThrow(() -> new BusinessException("用户不存在！"));
        BeanUtil.copyProperties(userInfoVo, user, CopyOptions.create().setIgnoreNullValue(true).setIgnoreError(true));
        userService.updateUser(user);
        Map<String, Object> userMap = UserTransUtils.getUserMap(user);
        redisProcessor.set(RedisTransKey.getLoginKey(user.getEmail()), userMap, 7, TimeUnit.DAYS);
        redisProcessor.set(RedisTransKey.getTokenKey(user.getEmail()),userMap,7,TimeUnit.DAYS);
        redisProcessor.set(RedisTransKey.getRefreshTokenKey(user.getEmail()),userMap,7,TimeUnit.DAYS);
        return ResponseResult.success("用户信息更新成功");
    }

    /**
     * 修改用户密码
     * @param oldPassword
     * @param newPassword
     * @return
     * @time 2024-09-13 16:53
     */

    @PutMapping("/update/password")
    public ResponseResult<String> updatePassword(String oldPassword, String newPassword) {
        if (StringUtil.isEmpty(oldPassword) || StringUtil.isEmpty(newPassword)) {
            return ResponseResult.fail("输入密码不能为空");
        }
        Long userId = currentUserHolder.getUserId();
        User user = userService.selectUserByUserId(userId)
                .orElseThrow(() -> new BusinessException("用户不存在！"));
        if (!SecurityUtils.checkPassword(oldPassword, user.getPassword())){
            return ResponseResult.fail("密码错误，请重新输入");
        }
        user.setPassword(SecurityUtils.encodePassword(newPassword));
        userService.updateUser(user);
        return ResponseResult.success("密码修改成功");
    }

    /**
     * 获取用户主页信息
     *
     * @return ResponseResult
     * @Time 2024-09-13 15:29
     * @author zhang
     */
    @GetMapping("/home")
    public ResponseResult<UserInfoVo> getProfile() {
        Long userId = currentUserHolder.getUserId();
        User user = userService.selectUserByUserId(userId)
                .orElseThrow(() -> new BusinessException("用户不存在！"));
        UserInfoVo userInfoVo = new UserInfoVo();
        BeanUtil.copyProperties(user, userInfoVo);
        return ResponseResult.success(userInfoVo);
    }


}

```

注意`PageHelper`分页时内部使用`ThreadLocal`会出现的问题：因为`PageHelper`将分页信息存储在`ThreadLocal`中，并且在调用后就会清理，因此在使用时要将分页查询的语句紧跟着分页设置语句，中间不要穿插其他查询语句。不然会导致其他查询语句将ThreadLocal中的分页信息用掉。正确示例如下：

```java
PageHelper.startPage(pageNo,pageSize);
List<User> users = userMapper.selectUsersByNickName(nickName);
```



## UserService

```java
package com.blog.service;

import com.blog.constant.Constant;
import com.blog.entity.User;
import com.blog.enums.ErrorCode;
import com.blog.exception.BusinessException;
import com.blog.mapper.UserMapper;
import com.blog.util.*;
import com.blog.util.bo.EmailCodeBo;
import com.blog.util.bo.LoginResponse;
import com.blog.util.redis.RedisTransKey;
import com.blog.util.redis.RedisProcessor;
import com.blog.vo.user.Loginer;
import com.blog.vo.user.Register;
import com.github.pagehelper.PageHelper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@AllArgsConstructor
public class UserService {
    private final UserMapper userMapper;

    private final RedisProcessor redisProcessor;

    private final JwtProcessor jwtProcessor;



    /**
     * 用户登录，登陆成功后返回accessToke，refreshToken，userId
     *
     * @Param loginer
     * @Return String
     * @Desription 用户登录
     */
    public LoginResponse userLogin(Loginer loginer) {
        String email = loginer.getEmail();
        String password = loginer.getPassword();
        //用户此时不为null，是否为null已经在selectUserByEmail判断
        User user = selectUserByEmail(email);
        boolean loginFlag = SecurityUtils.checkPassword(password, user.getPassword());
        if (!loginFlag) {
            log.error("密码错误，登陆失败，请重新输入");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误，登陆失败，请重新输入");
        }
        /*将用户信息存放在token中，时效为7天*/
        Map<String, Object> userMap = UserTransUtils.getUserMap(user);
        String accessToken = jwtProcessor.generateToken(userMap);
        String refreshToken = jwtProcessor.generateRefreshToken(userMap);
        redisProcessor.set(RedisTransKey.refreshTokenKey(email), refreshToken, 7, TimeUnit.DAYS);
        redisProcessor.set(RedisTransKey.tokenKey(email), accessToken, 7, TimeUnit.DAYS);
        redisProcessor.set(RedisTransKey.loginKey(email), email, 7, TimeUnit.DAYS);
        LoginResponse loginResponse = new LoginResponse(accessToken,refreshToken);
        return loginResponse;
    }

    /**
     * 刷新accessToken信息，并生成新的refreshToken
     *
     * @Param refreshToken
     * @Return Map<String, Object>
     */
    public String refreshAccessToken(String refreshToken, Long userId) {
        //对刷新令牌进行验证，以防恶意利用其他用户refreshToken刷新
        if (!jwtProcessor.validateToken(refreshToken, userId)) {
            log.error("refreshToken验证失败,当前用户id：" + userId);
            throw new BusinessException(ErrorCode.TOKEN_ERROR, "refreshToken验证失败,当前用户id：" + userId);
        }
        User user = userMapper.selectByPrimaryKey(userId);
        //没过期生成一个新的token
        Map<String, Object> userMap = UserTransUtils.getUserMap(user);
        String accessToken = jwtProcessor.generateToken(userMap);
        String newRefreshToken = jwtProcessor.generateRefreshToken(userMap);
        redisProcessor.set(RedisTransKey.refreshTokenKey(user.getEmail()), newRefreshToken);
        redisProcessor.set(RedisTransKey.tokenKey(user.getEmail()), accessToken);
        return accessToken;
    }


    /**
     * 注册新用户，并最后清理redis中的验证码信息
     *
     * @Description 注册新用户
     * @Param register
     * @Return User
     */
    public void userRegister(@Validated Register register) {
        String account = register.getAccount();
        log.info("开始注册新用户：" + account);
        String nickName = register.getNickName();
        String password = register.getPassword();
        String checkPassword = register.getCheckPassword();
        String email = register.getEmail();
        String phone = register.getPhone();
        String emailCode = register.getEmailCode().trim();
        if (userMapper.findByEmail(email) != null) {
            log.error("邮箱已注册，请重新输入：{}", email);
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS, "邮箱已注册，请重新输入");
        }
        //验证两次输入密码是否一致
        if (!checkPassword.equals(password)) {
            log.error("两次输入密码不一致，注册失败：{}", account);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入密码不一致，请重新输入");
        }
        /*验证邮箱验证码*/
        EmailCodeBo emailCodeBo = Optional.ofNullable((EmailCodeBo) redisProcessor.get(RedisTransKey.getEmailKey(email))).orElseThrow(() -> new BusinessException(ErrorCode.PARAMS_ERROR, "请先获取验证码"));
        if (!emailCodeBo.getCode().equals(emailCode)) {
            log.error("邮箱验证码输入错误，注册失败：{}", account);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请确认邮箱验证码是否正确");
        }
        if (!emailCodeBo.getEmail().equals(email)) {
            log.error("邮箱输入错误，注册失败：{}", account);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请确认邮箱验证码是否正确");
        }

        /*封装用户*/
        User user = new User();
        Long userId = SnowFlakeUtil.nextId();
        user.setUserId(userId);
        user.setAccount(account);
        user.setNickName(nickName);
        String encodePassword = SecurityUtils.encodePassword(password);
        user.setPassword(encodePassword);
        user.setEmail(email);
        user.setPhone(phone);
        String baseHomePageUrl = Constant.USER_BASE_PATH + userId;
        user.setWebsite(baseHomePageUrl);
        /*添加用户到数据库,并清理redis中存放的验证码*/
        userMapper.insertUser(user);
        redisProcessor.del(RedisTransKey.getEmailKey(email));
    }

    /**
     * @Description 根据用户邮箱查找用户
     * @Param email
     * @Return User
     */
    public User selectUserByEmail(String email) {
        User user = userMapper.findByEmail(email);
        if (user == null) {
            log.error("邮箱{}未注册，请注册", email);
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "邮箱{}未注册，请注册", email);
        }
        return user;
    }

    /**
     * 根据用户昵称去查找用户信息
     *
     * @param nickName
     * @return
     */
    public List<User> selectUsersByNickName(String nickName, int pageNo, int pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        List<User> users = userMapper.selectUsersByNickName(nickName);
        if (users.isEmpty()) {
            log.error("未找到用户");
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "未找到用户");
        }
        return users;
    }

    /**
     * 查询所有用户
     * @return List<User>
     */
    public List<User> getUsers(int pageNo, int pageSize) {
        PageHelper.startPage(pageNo, pageSize);
        return userMapper.selectUsers();
    }


    /**
     * @Description 根据用户id查找用户
     * @Param userId
     * @Return User
     */
    public Optional<User> selectUserByUserId(Long userId) {
        return Optional.ofNullable(userMapper.selectByPrimaryKey(userId));
    }

    /**
     * @param userId
     * @description 根据用户id删除用户
     */
    public void deleteUserById(Long userId) {
        userMapper.deleteByPrimaryKey(userId);
    }

    /**
     * 更新用户信息
     *
     * @param user
     */
    public void updateUser(User user) {
        userMapper.updateByPrimaryKeySelective(user);
    }


    public int getTotalCount() {
        return userMapper.selectTotalCount();
    }
}

```

# Comment  

## CommentService

链表+递归获取当前博客下的所有评论。

```java
package com.blog.service;

import cn.hutool.core.collection.CollectionUtil;
import com.blog.util.bo.BlogCommentBo;
import com.blog.dto.PageRequest;
import com.blog.entity.BlogComment;
import com.blog.mapper.BlogCommentMapper;
import com.github.pagehelper.PageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class CommentService {
    private final BlogCommentMapper blogCommentMapper;
    public void addComment(BlogComment blogComment) {
        blogCommentMapper.insertSelective(blogComment);
    }

    public Optional<BlogComment> selectCommentById(Integer commentId) {
        return Optional.ofNullable(blogCommentMapper.selectByPrimaryKey(commentId));
    }

    public void deleteComment(Integer commentId) {
        blogCommentMapper.deleteByPrimaryKey(commentId);
    }

    public List<BlogCommentBo> queryCommentList(PageRequest pageRequest, Long blogId) {
        int pageSize = pageRequest.getPageSize();
        int pageNo = pageRequest.getPageNo();
        PageHelper.startPage(pageNo, pageSize);
        //所有一级评论集合
        List<BlogCommentBo> firstCommentList = blogCommentMapper.queryFirstCommentList(blogId);
        //其他所有评论集合
        List<BlogCommentBo> secondCommentList = blogCommentMapper.querySecondCommentList(blogId);
        //将所有的其他评论以链表的方式添加到一级评论
        List<BlogCommentBo> list = addAllNodes(firstCommentList, secondCommentList);
        return list;
    }


    private List<BlogCommentBo> addAllNodes(List<BlogCommentBo> firstCommentList, List<BlogCommentBo> secondCommentList) {
        List<BlogCommentBo> tempSecondList = new ArrayList<>(secondCommentList);

        for (BlogCommentBo comment : tempSecondList) {
            if (addNode(firstCommentList, comment)) {
                secondCommentList.remove(comment);
            }
        }

        return firstCommentList;
    }

    private boolean addNode(List<BlogCommentBo> firstCommentList, BlogCommentBo blogCommentBo) {
        for (BlogCommentBo commentBo : firstCommentList) {
            //判断该回复是否是当前评论的回复，是当前评论的回复，则在其下一节点添加
            if (commentBo.getCommentId().equals(blogCommentBo.getLastId())){
                commentBo.getNextNodes().add(blogCommentBo);
                return true;
            }else {
                //若不是当前评论的回复，则判断其下一节点是否为空，若不为空，则递归判断
                if (CollectionUtil.isNotEmpty(commentBo.getNextNodes())){
                    if (addNode(commentBo.getNextNodes(), blogCommentBo)){
                        return true;
                    }
                }
            }
        }
        return false;
    }
}

```

## CommentController

```java
package com.blog.controller;

import cn.hutool.core.bean.BeanUtil;
import com.blog.authentication.CurrentUserHolder;
import com.blog.util.bo.BlogCommentBo;
import com.blog.dto.PageRequest;
import com.blog.dto.PageResult;
import com.blog.entity.Blog;
import com.blog.entity.BlogComment;
import com.blog.entity.User;
import com.blog.exception.BusinessException;
import com.blog.exception.ResponseResult;
import com.blog.service.BlogService;
import com.blog.service.CommentService;
import com.blog.service.UserService;
import com.blog.vo.comment.CommentOutVo;
import com.blog.vo.comment.CommentInVo;
import lombok.RequiredArgsConstructor;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;
    private final UserService userService;
    private final BlogService blogService;
    private final CurrentUserHolder currentUserHolder;

    @PostMapping("/blog/comment")
    public ResponseResult<String> addComment(@RequestBody CommentVoIn commentVo) {
        User user = userService.selectUserByUserId(commentVo.getCommentatorId())
                .orElseThrow(()->new BusinessException("用户不存在！"));
        if (user.getStatus() == 1) {
            return ResponseResult.fail("该用户已被封禁，无法评论！");
        }
        Blog blog = blogService.getBlogById(commentVo.getBlogId())
                .orElseThrow(()->new BusinessException("该博客不存在！"));
        if (blog.getEnableComment() == 1) {
            return ResponseResult.fail("该博客已关闭评论功能！");
        }
        BlogComment blogComment = new BlogComment();
        BeanUtil.copyProperties(commentVo, blogComment);
        blogComment.setCommentator(user.getNickName());
        blogComment.setCommentatorId(user.getUserId());
        commentService.addComment(blogComment);
        return ResponseResult.success("评论成功！");
    }
    @DeleteMapping("/blog/delete")
    public ResponseResult<String> deleteComment(Integer commentId) {
        BlogComment blogComment = commentService.selectCommentById(commentId)
                .orElseThrow(()->new BusinessException("该评论不存在！"));
        Long commentatorId = blogComment.getCommentatorId();
        if (!currentUserHolder.getUserId().equals(commentatorId)) {
           return ResponseResult.fail("没有权限删除！");
        }
        commentService.deleteComment(commentId);
        return ResponseResult.success("删除成功！");
    }

    @GetMapping("/{blogId}")
    public ResponseResult<List<BlogCommentBo>> getCommentListAll(@PathVariable Long blogId, @RequestParam Map<String,Object> params) {
        if (ObjectUtils.isEmpty(params.get("pageNo")) || ObjectUtils.isEmpty(params.get("pageSize"))) {
            return ResponseResult.fail("参数异常！");
        }
        blogService.getBlogById(blogId).orElseThrow(() -> new BusinessException("该博客不存在！"));
        PageRequest pageRequest = new PageRequest(params);
        return ResponseResult.success(commentService.queryCommentList(pageRequest,blogId));
    }

}

```

注意：Optional在这里的使用，以及分页参数的封装！

# Admin

由于在`AdminController`中要实现的接口功能中，与`UserController`和`BlogController`几个接口功能相同。因此，将这几个接口的功能抽取到`BaseController`中，如下：

## BaseController

```java
package com.blog.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.blog.dto.PageRequest;
import com.blog.dto.PageResult;
import com.blog.entity.User;
import com.blog.exception.BusinessException;
import com.blog.exception.ResponseResult;
import com.blog.service.BlogService;
import com.blog.service.UserService;
import com.blog.vo.user.UserVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class BaseController {
    @Autowired
    private UserService userService;
    @Autowired
    private BlogService blogService;

    /**
     * 根据昵称查询用户
     * @param nickName
     * @param params
     * @return ResponseResult
     * @author zhang
     */
    @GetMapping("/{nickName}")
    public ResponseResult<PageResult<UserVo>> getUsersByNickName(@PathVariable String nickName, @RequestParam Map<String, Object> params) {
        if (CollectionUtil.isEmpty(params)){
            return ResponseResult.fail("分页参数为空");
        }
        PageRequest pageRequest = new PageRequest(params);
        List<User> users = userService.selectUsersByNickName(nickName, pageRequest.getPageNo(), pageRequest.getPageSize());
        List<UserVo> result = users.stream().map(user -> BeanUtil.copyProperties(user, UserVo.class))
                .collect(Collectors.toList());
        int totalCount = result.size();
        PageResult<UserVo> pageResult = new PageResult<>(result, totalCount);
        return ResponseResult.success(pageResult);
    }

    /**
     * 获取所有用户列表
     * @param params
     * @return ResponseResult
     * @author zhang
     */
    @GetMapping("/getUsers")
    public ResponseResult<Object> getUsers(@RequestParam Map<String, Object> params) {
        if (CollectionUtil.isEmpty(params)){
            return ResponseResult.fail("分页参数为空");
        }
        PageRequest pageRequest = new PageRequest(params);
        List<User> users = userService.getUsers(pageRequest.getPageNo(), pageRequest.getPageSize());
        List<UserVo> result = users.stream()
                .map(user -> BeanUtil.copyProperties(user, UserVo.class))
                .collect(Collectors.toList());
        int totalCount = userService.getTotalCount();
        PageResult<UserVo> pageResult = new PageResult<>(result, totalCount);
        return ResponseResult.success(pageResult);
    }

}
```

## AdminService

```java
package com.blog.service;

import com.blog.entity.Admin;
import com.blog.exception.BusinessException;
import com.blog.mapper.AdminMapper;
import com.blog.util.JwtProcessor;
import com.blog.util.bo.LoginResponse;
import com.blog.util.redis.RedisProcessor;
import com.blog.util.redis.RedisTransKey;
import com.blog.vo.admin.AdminInVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 管理员模块业务层
 *
 * @author : [24360]
 * @version : [v1.0]
 * @createTime : [2024/9/13 15:41]
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {
    private final AdminMapper adminMapper;
    private final JwtProcessor jwtProcessor;
    private final RedisProcessor redisProcessor;

    public LoginResponse adminLogin(AdminVoIn adminInVo) {
        Admin admin = Optional.ofNullable(adminMapper.selectByAccount(adminInVo.getAccount()))
                .orElseThrow(() -> new BusinessException("该管理员账号不存在"));
        if (!admin.getPassword().equals(adminInVo.getPassword())) {
            throw new BusinessException("密码错误");
        }
        Map<String, Object> adminMap = new HashMap<>();
        adminMap.put("account",admin.getAccount());
        adminMap.put("id",admin.getAdminId());
        String accessToken = jwtProcessor.generateToken(adminMap);
        String refreshToken = jwtProcessor.generateRefreshToken(adminMap);

        redisProcessor.set(RedisTransKey.tokenKey(admin.getAccount()),accessToken,7, TimeUnit.DAYS);
        redisProcessor.set(RedisTransKey.refreshTokenKey(admin.getAccount()),refreshToken,7, TimeUnit.DAYS);

        return new LoginResponse(accessToken, refreshToken);
    }
    public Optional<Admin> getAdminById(Long adminId) {
        return Optional.ofNullable(adminMapper.selectByPrimaryKey(adminId));
    }

    public void updateAdmin(Admin admin) {
        adminMapper.updateByPrimaryKeySelective(admin);
    }
}
```

## AdminController

```java
package com.blog.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.blog.authentication.CurrentUserHolder;
import com.blog.entity.Admin;
import com.blog.entity.User;
import com.blog.enums.ErrorCode;
import com.blog.exception.BusinessException;
import com.blog.exception.ResponseResult;
import com.blog.service.AdminService;
import com.blog.service.BlogService;
import com.blog.service.UserService;
import com.blog.util.JwtProcessor;
import com.blog.util.bo.LoginResponse;
import com.blog.util.redis.RedisProcessor;
import com.blog.util.redis.RedisTransKey;
import com.blog.vo.admin.AdminInVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;



@RequestMapping("/admin")
@RestController
@RequiredArgsConstructor
public class AdminController extends BaseController{
    private final UserService userService;
    private final AdminService adminService;
    private final BlogService blogService;
    private final CurrentUserHolder currentUserHolder;
    private final JwtProcessor jwtProcessor;
    private final RedisProcessor redisProcessor;


    /**
     * 管理员账号登录
     * @param adminInVo
     * @return ResponseResult
     * @author zhang
     */
    @PostMapping("/login")
    public ResponseResult<LoginResponse> login(@RequestBody AdminVoIn adminInVo) {
        LoginResponse loginResponse = adminService.adminLogin(adminInVo);
        return ResponseResult.success(loginResponse);
    }

    /**
     * 更新管理员账号信息
     * @param adminInVo
     * @return ResponseResult
     * @author zhang
     */
    @PutMapping("/update")
    public ResponseResult<String> updateAdmin(@RequestBody AdminVoIn adminInVo) {
        String accessToken = (String) redisProcessor.get(RedisTransKey.getTokenKey(adminInVo.getAccount()));
        Long adminId = currentUserHolder.getUserId();
        if (!jwtProcessor.validateToken(accessToken, adminId)) {
            return ResponseResult.fail(ErrorCode.TOKEN_ERROR.getCode(), "token验证失败");
        }
        Admin admin = adminService.getAdminById(adminId).orElseThrow(() -> new BusinessException("该管理员账户不存在"));

        BeanUtil.copyProperties(adminInVo, admin, CopyOptions.create().setIgnoreNullValue(true).setIgnoreCase(true));
        adminService.updateAdmin(admin);
        return ResponseResult.success("管理员信息更新成功");
    }


    /**
     * 根据用户id查找用户
     * @param userId
     * @return ResponseResult
     * @author zhang
     */
    @GetMapping("/user/{userId}")
    public ResponseResult<User> getUser(@PathVariable Long userId) {
        return ResponseResult.success(userService.selectUserByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND, "用户不存在")));
    }

    /**
     * 删除用户信息
     * @param userId
     * @return ResponseResult
     * @author zhang
     */
    @DeleteMapping("/user/delete/{userId}")
    public ResponseResult<String> delete(@PathVariable Long userId) {
        userService.selectUserByUserId(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        userService.deleteUserById(userId);
        return ResponseResult.success("用户删除成功");
    }

    /**
     * 修改用户状态，0为正常 1为封禁
     * @param userId
     * @param status
     * @return
     */
    @PutMapping("/user/status")
    public ResponseResult<String> updateUserStatus(@RequestParam Long userId, @RequestParam Integer status) {
        User user = userService.selectUserByUserId(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        user.setStatus(status);
        userService.updateUser(user);
        return ResponseResult.success("用户状态更新成功");
    }

    /**
     * 根据博客id删除博客
     * @param blogId
     * @return ResponseResult
     * @author zhang
     */
    @DeleteMapping("/delete/{blogId}")
    public ResponseResult<String> deleteBlog(@PathVariable Long blogId) {
        blogService.getBlogById(blogId)
                .orElseThrow(() -> new BusinessException("文章不存在！"));
        blogService.deleteBlog(blogId);
        return ResponseResult.success("删除成功");
    }


}
```

# Controller单元测试

在`controller`层进行单元测试发起请求有以下几种方式(重点在第四个方法)：

### 1. 使用`WebClient`

Spring 5引入了`WebClient`，这是一个非阻塞的、反应式的客户端来发送HTTP请求。以下是如何在`JUnit`测试中使用`WebClient`：

```java
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebClientIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private WebClient webClient;

    @Test
    public void testYourEndpoint() {
        Mono<String> response = webClient.get()
                .uri("http://localhost:" + port + "/your-endpoint")
                .retrieve()
                .bodyToMono(String.class);

        StepVerifier.create(response)
                .expectNext("Expected Response")
                .verifyComplete();
    }
}
```

### 2. 使用Apache HttpClient

Apache HttpClient是一个广泛使用的HTTP客户端库。以下是如何在JUnit测试中使用它：

```java
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class HttpClientTest {

    @Test
    public void testYourEndpoint() throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet("http://localhost:8080/your-endpoint");

        try (CloseableHttpClient client = httpClient) {
            CloseableHttpResponse response = client.execute(request);
            assertEquals(200, response.getStatusLine().getStatusCode());
            String responseBody = EntityUtils.toString(response.getEntity());
            assertEquals("Expected Response", responseBody);
        }
    }
}
```

### 3. 使用OkHttp

OkHttp是一个适用于Java应用程序的HTTP客户端。以下是如何在JUnit测试中使用OkHttp：

```java
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class OkHttpTest {

    @Test
    public void testYourEndpoint() throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://localhost:8080/your-endpoint")
                .build();

        try (Response response = client.newCall(request).execute()) {
            assertEquals(200, response.code());
            assertEquals("Expected Response", response.body().string());
        }
    }
}
```

### 4. 使用JUnit 5的`@HttpTest`

JUnit 5提供了一个实验性的`@HttpTest`注解，它结合了`RestTemplate`和嵌入式的服务器。以下是如何使用它：

```java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class HttpTestExample {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testYourEndpoint() {
        ResponseEntity<String> response = restTemplate.getForEntity("/your-endpoint", String.class);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Expected Response", response.getBody());
    }
}
```

#### 上述4方法代码详解
1. **注解**：
   - `@ExtendWith(SpringExtension.class)`: 这是JUnit 5的注解，用于集成Spring Test框架。它允许Spring Test的注解和JUnit 5的注解一起使用。
   - `@SpringBootTest`: 这个注解用于指示JUnit加载Spring Boot应用程序上下文。`webEnvironment = WebEnvironment.RANDOM_PORT`属性指定测试应该在一个随机端口上启动嵌入式服务器。
2. **类和成员变量**：
   - `TestRestTemplate restTemplate`: 这是一个Spring Boot提供的特殊`RestTemplate`实现，用于测试。它被注入到测试类中，以便发送HTTP请求。
3. **测试方法**：
   - `@Test`: 标记这个方法为一个测试方法。
   - `testYourEndpoint()`: 这个方法使用`TestRestTemplate`发送一个GET请求到`/your-endpoint`，并验证响应状态和内容。
#### 为什么URL路径可以不使用完整的URL路径？
在测试方法中，使用`restTemplate.getForEntity("/your-endpoint", String.class);`发送请求时，没有指定完整的URL（如`http://localhost:port/your-endpoint`），这是因为：
1. **随机端口**：由于`@SpringBootTest`注解的`webEnvironment = WebEnvironment.RANDOM_PORT`属性，Spring Boot会启动一个嵌入式服务器，并随机选择一个可用端口。`TestRestTemplate`自动配置了该端口，因此不需要在URL中指定。
2. **基础URL**：`TestRestTemplate`在内部已经配置了基础URL，即`http://localhost:port`（其中`port`是随机分配的端口）。因此，当你在测试中只提供路径（如`/your-endpoint`）时，`TestRestTemplate`会自动将基础URL与提供的路径结合起来，形成一个完整的URL进行请求。
使用这种简写方式，可以使测试代码更加简洁，并减少在测试中硬编码URL的需要。这也意味着，如果端口发生变化（例如，因为随机分配），测试代码不需要修改，因为它依赖于Spring Boot提供的自动配置。
总的来说，Spring Boot测试框架通过`TestRestTemplate`和其他自动配置特性，简化了集成测试的编写过程，允许开发者专注于测试逻辑，而不是测试环境配置。
