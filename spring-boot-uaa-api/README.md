前后端分离-使用简单模式访问受限资源
=====

### spring-boot-api
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
### spring-boot-ui
spring boot ui 是纯静态应用，由html+js组成。
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