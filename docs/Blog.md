

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

`UserController.refreshToken`方法中新增了对当前用户状态是否异常的判断，若为异常用户，则无法刷新token

```java
    /**
     * 刷新token
     *
     * @return accessToken
     */
    @PostMapping("/refresh")
    public ResponseEntity<String> refreshToken() {
        Long userId = currentUserHolder.getUserId();
        User user = userMapper.selectByPrimaryKey(userId);
        if (user.getStatus() == 0){
            log.error("该用户处于异常状态，无法执行下一步操作");
            throw new BusinessException("该用户处于异常状态，无法执行下一步操作");
        }
        String refreshToken = (String) redisProcessor.get(RedisTransKey.getRefreshTokenKey(user.getEmail()));
        String accessToken = userService.refreshAccessToken(refreshToken);
        //返回给前端刷新后的accessToken,同时也会产生新的refreshToken,以防refreshToken过期
        return ResponseEntity.ok(accessToken);
    }

```



## TokenProcessor内部异常处理

对公用方法`validateToken`和`extractUserId`进行了`token`异常处理，在`isTokenExpired`方法中新增了invalidToken异常处理。

```java
public Boolean validateToken(String token, Long userId) {
    if (token==null){
        log.error("token为空");
        throw new BusinessException(ErrorCode.PARAMS_ERROR,"token为空");
    }
    if (isTokenExpired(token)){
        log.error("token已失效");
        throw new BusinessException(ErrorCode.TOKEN_EXPIRED,"token已失效");
    }
    final Long userIdFromToken = extractUserId(token);
    return (userIdFromToken.equals(userId) && !isTokenExpired(token));
}
public Long extractUserId(String token) {
    if (token==null){
        log.error("token为空");
        throw new BusinessException(ErrorCode.PARAMS_ERROR,"token为空");
    }
    if (isTokenExpired(token)){
        log.error("token已失效");
        throw new BusinessException(ErrorCode.TOKEN_EXPIRED,"token失效");
    }
    return Long.valueOf(extractClaim(token, Claims::getSubject));
}
private Boolean isTokenExpired(String token) {
    boolean ret;
    try {
        ret = extractExpiration(token).before(new Date());
    }catch (Exception e){
        log.error("token解析失败,invalid token");
        throw new BusinessException(ErrorCode.TOKEN_ERROR,"token解析失败,invalid token");
    }
    return ret;
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

可以转换`VO`对象和`entity`对象，很方便。但是也可以直接使用`BeanUtils.copyProperties`方法。

```java


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

更新博客时只copy `bean`中的非null属性，这种情况可使用`hutool`中的`BeanUtil`工具，如下：

```java
BeanUtil.copyProperties(oldDetail, userDetail, 
    CopyOptions.create().setIgnoreNullValue(true).setIgnoreError(true));
```



## BlogController  saveBlog+updateBlog

```java
@PostMapping("/save")
public ResponseEntity<String> saveBlog(@RequestBody Blog blog){
    blogService.saveBlog(blog);
    return ResponseEntity.ok("文章保存成功");
}
```

```java
@PutMapping("/update")
public ResponseEntity<String> updateBlog(@RequestBody Blog blog){
    blogService.updateBlog(blog);
    return ResponseEntity.ok("文章更新成功");
}
```

## BlogVo

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
@GetMapping("/profile")
public ResponseEntity<User> getProfile() {
    Long userId = currentUserHolder.getUserId();
    User user = userService.selectUserByUserId(userId);
    return ResponseEntity.ok(user);
}

@GetMapping("/{email}")
public ResponseEntity<String> getUserWebByEmail(@PathVariable String email) {
    User user = userService.selectUserByEmail(email);
    String website = user.getWebsite();
    return ResponseEntity.ok(website);
}

@GetMapping("/{nickName}")
public ResponseEntity<List<String>> getUserWebByNickName(@PathVariable String nickName, @RequestParam int pageNo, @RequestParam int pageSize) {
    List<User> users = userService.selectUsersByNickName(nickName,pageNo,pageSize);
    List<String> result = new ArrayList<>();
    for (User user : users) {
        result.add(user.getWebsite());
    }
    return ResponseEntity.ok(result);
}
```

注意`PageHelper`分页时内部使用`ThreadLocal`会出现的问题：因为`PageHelper`将分页信息存储在`ThreadLocal`中，并且在调用后就会清理，因此在使用时要将分页查询的语句紧跟着分页设置语句，中间不要穿插其他查询语句。不然会导致其他查询语句将ThreadLocal中的分页信息用掉。正确示例如下：

```java
PageHelper.startPage(pageNo,pageSize);
List<User> users = userMapper.selectUsersByNickName(nickName);
```



## UserService

```java
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
        log.info("查找用户成功");
        return user;
    }

    /**
     * 根据用户昵称去查找用户信息
     * @param nickName
     * @return
     */
    public List<User> selectUsersByNickName(String nickName, int pageNo, int pageSize){
        PageHelper.startPage(pageNo,pageSize);
        List<User> users = userMapper.selectUsersByNickName(nickName);
        if (users.isEmpty()){
            log.error("未找到用户");
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "未找到用户");
        }
        log.info("查找用户成功")
        return users;
    }

    /**
     * @Description 根据用户id查找用户
     * @Param userId
     * @Return User
     */
    public User selectUserByUserId(Long userId) {
        User user = userMapper.selectByPrimaryKey(userId);
        log.info("查找用户成功");
        return user;
    }
```

# Comment

CommentService.java

```java
package com.blog.service;

import com.blog.dto.PageRequest;
import com.blog.dto.PageResult;
import com.blog.entity.BlogComment;
import com.blog.mapper.BlogCommentMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    public PageResult<BlogComment> getCommentList(PageRequest pageRequest, Long blogId) {
        int pageSize = pageRequest.getPageSize();
        int pageNo = pageRequest.getPageNo();
        PageHelper.startPage(pageNo, pageSize);
        List<BlogComment> blogCommentList = blogCommentMapper.selectByBlogId(blogId);
        int totalCount = blogCommentMapper.selectCommentCountByBlogId(blogId);
        return new PageResult<>(blogCommentList, totalCount);
    }
}
```

CommentController.java

```java
package com.blog.controller;

import cn.hutool.core.bean.BeanUtil;
import com.blog.authentication.CurrentUserHolder;
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
import com.blog.vo.comment.CommentInfo;
import com.blog.vo.comment.CommentVo;
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

    @PostMapping("/blog/addComment")
    public ResponseResult<String> addComment(@RequestBody CommentVo commentVo) {
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
    public ResponseResult<PageResult<CommentInfo>> getCommentList(@PathVariable Long blogId, @RequestParam Map<String,Object> params) {
        if (ObjectUtils.isEmpty(params.get("pageNo")) || ObjectUtils.isEmpty(params.get("pageSize"))) {
            return ResponseResult.fail("参数异常！");
        }
        PageRequest pageRequest = new PageRequest(params);
        PageResult<BlogComment> blogCommentPageResult = commentService.getCommentList(pageRequest, blogId);
        List<CommentInfo> commentInfoList = blogCommentPageResult.getList().stream()
                .map(blogComment -> BeanUtil.copyProperties(blogComment, CommentInfo.class))
                .collect(Collectors.toList());
        PageResult<CommentInfo> commentInfoPageResult = new PageResult<>(commentInfoList, blogCommentPageResult.getTotalCount());
        return ResponseResult.success(commentInfoPageResult);
    }
}

```

注意：Optional在这里的使用，以及分页参数的封装！
