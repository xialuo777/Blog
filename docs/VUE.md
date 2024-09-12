# VUE

基础上手学习(https://ssegsa.github.io/practice/vue/vue-login-demo.html)

## 登录

Token 参考1  

这里要注意前端存放accessToken与后端取accessToken要一致。

accessToken存放前端：

```vue
methods: {
submitForm(formName) {
  //验证输入邮箱以及密码是否有效
  this.$refs[formName].validate((valid) => {
    //点击登陆后，展示加载动画
    this.loading = true;
    //如果校验通过，则发送请求给后端登录接口
    if (valid) {
      let _this = this;
      //使用axios将登陆信息发送到后端
      this.axios({
        url: "/api/users/login",                  //请求地址
        method: "post",                           //请求方法
        headers: {
          "Content-Type": "application/json",     //请求头
        },
        data: {
          email: _this.ruleForm.email,
          password: _this.ruleForm.password,
        },
      }).then((res) => {        //当收到后端响应时执行括号内代码，其中res为相应信息
        if (res.data.code === 20000) { //当相应的编码为20000时，说明登陆成功
          //将相应的accessToken和refreshToken存储到sessionStorage中
          sessionStorage.setItem("token", JSON.stringify(res.data.data));
          console.log(sessionStorage.getItem("token"))
          //跳转页面到首页
          this.$router.push('/home');
          //显示后端相应的成功信息
          this.$message({
            message: res.data.msg,
            type: "success",
          });
        } else {
          //显示响应失败的信息
          this.$message({
            message: res.data.msg,
            type: "warning"
          });
        }
        //后端响应信息后，关闭登录按钮加载动画
        _this.loading = false;
        console.log(res);
      });
    } else {  //如果邮箱和密码有一个没填，则提示，并不用向后端发送请求
      console.log("error submit!!");
      this.loading = false;
      return false;
    }
  });
},
```

前端将accessToken放在请求头中向后端发起请求，两种方式

方式一：

```vue
fetchUserInfo() {
  this.makeRequest('/api/users/home', 'get')
      .then(response => {
        if (response.data.code === 20000) {
          const userData = response.data.data;
          console.log(userData);
          this.user = {
            uname: userData.nickName, // 使用nickName作为用户名显示
            bio: '暂无简介', // 假设后端没有提供个人简介，可以设置默认值
            email: userData.email,
            phone: userData.phone,
          };
          this.fetchUserArticles(); // 在获取用户信息后获取文章列表
        } else {
          console.error('获取用户信息失败');
        }
      })
      .catch(error => {
        console.error('请求错误', error);
      });
},
// 通用请求函数
makeRequest(url, method, data = null, config = {}) {
  // 从sessionStorage获取accessToken
  const token = sessionStorage.getItem('token');
  console.log(token);
  if (token) {
    const accessToken = JSON.parse(token).accessToken;
    console.log(accessToken);
    // 将accessToken添加到请求头
    const headers = { ...config.headers, Authorization: `${accessToken}` };
    return axios({ url, method, data, headers });
  } else {
    // 如果没有accessToken，可以处理未登录的逻辑
    console.error('未找到accessToken');
  }
},
```

方式二：

```vue
<script>
import axios from "axios";

export default {
  data() {
    return {
      total: 0,
      now: 1,
      size: 8,
      input2: "",
      tableDatas: [],
      userInfo: null, // 存储用户信息
    };
  },
  methods: {
    handleClick(row) {
      console.log(row);
    },

    // 第n页信息
    findPage(now_page) {
      this.now = now_page;
      this.showAllUserInfo(now_page, this.size);
    },
    findSize(now_size) {
      this.size = now_size;
      this.showAllUserInfo(this.now, now_size);
    },

    // 展示所有的用户信息
    showAllUserInfo(currentPage, pageSize) {
      currentPage = currentPage ? currentPage : this.now;
      pageSize = pageSize ? pageSize : this.size;
      const headers = {
        'Authorization': `Bearer ${this.userInfo.accessToken}` // 设置请求头
      };
      axios
          .get("/api/users/getUsers" + "?pageNo=" + currentPage + "&pageSize=" + pageSize, { headers })
          .then((res) => {
            this.tableDatas = res.data.data.result.userList;
            this.total = res.data.data.result.totals;
          });
    },
  },
  created() {
    this.userInfo = sessionStorage.getItem('userInfo') ? JSON.parse(sessionStorage.getItem('userInfo')) : null;
    this.showAllUserInfo();
  },
};
</script>
```

```java
String accessToken = request.getHeader("Authorization");
```



## 注册



## 用户列表

普通用户搜索列表

```vue

<template>

  <div>
    <!-- 搜索区域 -->
    <div class="filter-container sousuo">
      <el-input v-model="listQuery.nickName" placeholder="根据昵称查询" style="width: 200px;"/>
      <el-button type="primary" icon="el-icon-search" @click="searchData">
        查询
      </el-button>
    </div>

    <!-- 用户列表 -->
    <el-table v-loading="listLoading" :data="userList" element-loading-text="Loading" border stripe fit highlight-current-row>
      <el-table-column label="用户ID">
        <template v-slot="scope">
          {{ scope.row.userId }}
        </template>
      </el-table-column>
      <el-table-column label="昵称">
        <template v-slot="scope">
          {{ scope.row.nickName }}
        </template>
      </el-table-column>
      <el-table-column label="邮箱" align="center">
        <template v-slot="scope">
          <span>{{ scope.row.email }}</span>
        </template>
      </el-table-column>
      <el-table-column label="网址" align="center">
        <template v-slot="scope">
          <span>{{ scope.row.website }}</span>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
          background
          :current-page="pagination.currentPage"
          :page-sizes="[5, 10, 20, 40]"
          :page-size="pagination.pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.totalCount"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange">
      </el-pagination>
    </div>
  </div>
</template>

<script>
import axios from "axios";

export default {
  data() {
    return {
      user: {
        uname: "",
        uid: null,
      },
      pagination: {
        currentPage: 1, //初始页
        pageSize: 10, //每页的数据
        totalCount: 0 //总数据
      },
      userList: [],
      listLoading: false,
      addUserFormVisible: false, // 控制添加用户的表单是否可见
      // 搜索条件
      listQuery: {
        nickName: "",
        pageNum: 1,
        pageSize: 10
      },
      accessToken: "", // 存储accessToken
    };
  },
  methods: {
    // 改变分页的每页的页数
    handleSizeChange(size) {
      this.pagination.pageSize = size;
      this.listQuery.pageSize = size;
      this.getUserList();
    },
    // 改变分页的当前页面
    handleCurrentChange(currentPage) {
      this.pagination.currentPage = currentPage;
      this.listQuery.pageNum = currentPage;
      this.getUserList();
    },
    // 查询
    searchData() {
      this.listQuery.pageNum = 1;
      this.pagination.currentPage = 1;
      this.getUserList();
    },
    // 获取用户列表
    getUserList() {
      this.listLoading = true;
      const url = this.listQuery.nickName ? `/api/users/${this.listQuery.nickName}` : "/api/users/getUsers";
      // 从 sessionStorage 中获取 accessToken
      const token = sessionStorage.getItem('token');
      const accessToken = token ? JSON.parse(token).accessToken : null;

      // 构建请求头
      const headers = {
        'Authorization': accessToken ? `${accessToken}` : ''
      };

      axios.get(url, {
        params: {
          pageNo: this.listQuery.pageNum,
          pageSize: this.listQuery.pageSize
        },
        headers: headers // 将请求头添加到请求中
      }).then(response => {
        this.listLoading = false;
        if (response.data.code === 20000) {
          this.userList = response.data.data.list; // 更新数据列表
          this.pagination.totalCount = response.data.data.totalCount; // 更新总数据量
        } else {
          this.$message.error(response.data.msg);
        }
      }).catch(error => {
        this.listLoading = false;
        console.error('请求错误', error);
      });
    },
  },
  created() {
  },
  logout(){
    // 移除本地用户登录信息
    sessionStorage.removeItem('token');
    // 跳转页面到登录页
    this.$router.push('/login');
  },

  mounted() {
  },
};

</script>
```



管理员用户列表

```vue

<template>
  <div>
    <!-- 搜索区域 -->
    <div class="filter-container sousuo">
      <el-input v-model="listQuery.email" placeholder="根据邮箱查询" style="width: 200px;"/>
      <el-button type="primary" icon="el-icon-search" @click="searchData">
        查询
      </el-button>
      <el-button style="margin-left: 10px;" type="primary" icon="el-icon-edit" @click="addUserFormVisible = true">
        添加
      </el-button>
    </div>

    <!-- 用户列表 -->
    <el-table v-loading="listLoading" :data="userList" element-loading-text="Loading" border stripe fit highlight-current-row>
      <el-table-column label="用户ID">
        <template slot-scope="scope">
          {{ scope.row.userId }}
        </template>
      </el-table-column>
      <el-table-column label="昵称">
        <template slot-scope="scope">
          {{ scope.row.nickName }}
        </template>
      </el-table-column>
      <el-table-column label="邮箱" align="center">
        <template slot-scope="scope">
          <span>{{ scope.row.email }}</span>
        </template>
      </el-table-column>
      <el-table-column label="网址" align="center">
        <template slot-scope="scope">
          <span>{{ scope.row.website }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" width="230" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="danger" @click="deleteUser(scope.row.userId)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
          background
          :current-page="pagination.currentPage"
          :page-sizes="[5, 10, 20, 40]"
          :page-size="pagination.pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.totalCount"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange">
      </el-pagination>
    </div>
  </div>
</template>

<script>
import axios from "axios";

export default {
  data() {
    return {
      pagination: {
        currentPage: 1, //初始页
        pageSize: 10, //每页的数据
        totalCount: 0 //总数据
      },
      userList: [],
      listLoading: true,
      addUserFormVisible: false, // 控制添加用户的表单是否可见
      // 搜索条件
      listQuery: {
        email: undefined,
        pageNum: 1,
        pageSize: 10
      },
    };
  },
  methods: {
    // 改变分页的每页的页数
    handleSizeChange(size) {
      this.pagination.pageSize = size;
      this.listQuery.pageSize = size;
      this.getUserList();
    },
    // 改变分页的当前页面
    handleCurrentChange(currentPage) {
      this.pagination.currentPage = currentPage;
      this.listQuery.pageNum = currentPage;
      this.getUserList();
    },
    // 查询
    searchData() {
      this.listQuery.pageNum = 1;
      this.pagination.currentPage = 1;
      this.getUserList();
    },
    // 获取用户列表
    getUserList() {
      this.listLoading = true;
      axios.get("/api/users/getUsers", {
        params: {
          pageNo: this.listQuery.pageNum,
          pageSize: this.listQuery.pageSize
        }
      }).then(response => {
        this.listLoading = false;
        if (response.data.code === 20000) {
          this.userList = response.data.data; // 更新数据列表
          console.log(response.data)
          this.pagination.totalCount = response.data.data.length; // 更新总数据量
        } else {
          this.$message.error(response.data.msg);
        }
      }).catch(error => {
        this.listLoading = false;
        console.error('请求错误', error);
      });
    },
    // 删除用户
    deleteUser(userId) {
      this.$confirm('确定要删除此用户吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        axios.delete(`/api/users/${userId}`).then(response => {
          if (response.data.code === 20000) {
            this.$message.success('删除成功');
            this.getUserList(); // 重新获取用户列表
          } else {
            this.$message.error(response.data.msg);
          }
        });
      }).catch(() => {
        this.$message.info('已取消删除');
      });
    }
  },
  created() {
    this.getUserList(); // 初始化时获取用户列表
  },
};
</script>
```

## 列表显示

在列表显示时要注意使前端字段与后端字段保持一致
