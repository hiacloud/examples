前后端分离-访问受限资源
=====

## spring-boot-api
作为资源服务器:
```
@Configuration
@EnableResourceServer
public class SecurityConfiguration {}
```
服务API：
```
@RequestMapping("/user")
public Principal user(Principal principal) {
	return principal;
}
```
## spring-boot-ui
spring boot ui 是纯静态应用，由html+js组成。这时候访问受限资源通常有两种方式：简单模式或password模式

### 简单模式 index.html
```
<script type="text/javascript">
    var appID = "zhuqinghua";
    var hash;
    function fbLogin() {
        // 使用简单模式获取token, 在返回的url的hash中存在token
        var path = 'http://192.168.66.158:18080/oauth/authorize?';
        var queryParams = ['client_id=' + appID,
            'redirect_uri=' + window.location,
            'response_type=token'];
        var query = queryParams.join('&');
        var url = path + query;
        window.location.replace(url);
    }
    // 使用token，访问后台受限资源/api/user
    function checkFbHashLogin() {
        if (window.location.hash.length > 3) {
            hash = window.location.hash.substring(1);
            if (hash.split('=')[0] == 'access_token') {
                var path = "http://localhost:489/api/user?";
                var url = path + hash;

                $.ajax({
                    dataType: "json",
                    type: "GET",
                    url: url,
                    success: function (data) {
                        displayUser(data);
                    }
                });
            }
        }
    }

    function displayUser(data) {
        $("#user").html(data.name);
        $(".unauthenticated").hide()
        $(".authenticated").show()
    }

    checkFbHashLogin();

    $('#login').click(function () {
        fbLogin();
    });
    $('#logout').click(function () {
        hash = undefined;
        $("#user").html("");
        $(".unauthenticated").show()
        $(".authenticated").hide()
    });
</script>
```

### password模式 index2.html
```
<script type="text/javascript">
    var appID = "zhuqinghua";
    var token;
    function fbLogin() {
        var path = '/oauth/token';
		var dataParams = [
			'client_id=' + appID,
			'client_secret=' + appID,
            'username=admin',
            'password=111111',
            'grant_type=password'];
        var query = dataParams.join('&');
		$.ajax({  
           type: "post",  
           url: path,  
           contentType : "application/x-www-form-urlencoded; charset=UTF-8",  
           data: query,  
           success: function (res) {  
			  token = res.access_token
              // alert(res.access_token);
			  getUser();			  
           }  
       });         
    }
    function getUser() {
		var url = "/api/user?";
		$.ajax({
			url: url,
			dataType: "json",
			type: "GET",
			headers:{
				Authorization: 'beaerer '+ token
			},			
			success: function (data) {
				displayUser(data);
			}
		});
    }

    function displayUser(data) {
        $("#user").html(data.name);
        $(".unauthenticated").hide()
        $(".authenticated").show()
    }


    $('#login').click(function () {
        fbLogin();
    });
    $('#logout').click(function () {
        hash = undefined;
        $("#user").html("");
        $(".unauthenticated").show()
        $(".authenticated").hide()
    });
</script>
```

nginx配置：
```
	server {
        listen       489;
        server_name  localhost;
		location ^~/api/ {
			set	$prerender 'http://127.0.0.1:8080'; 			
			proxy_pass 	$prerender;
		}
		location ^~/oauth/ {
			set	$prerender 'http://192.168.66.158:18080';
			proxy_pass 	$prerender;
		}
	
		# 其他
        location / {           		
            root    C:/Users/lenovo/Desktop/work/webfront;				
            index  index.html index.htm;
        }
	}
```