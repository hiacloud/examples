server {
        listen       489;
        server_name  localhost;
		location ^~/api/ {
			set	$prerender 'http://127.0.0.1:8080'; 			
			proxy_pass 	$prerender;
		}
	
		# ÆäËû
        location / {           		
            root    C:/Users/lenovo/Desktop/work/webfront;				
            index  index.html index.htm;
        }
}