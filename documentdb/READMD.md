# aws documentdb access

- ssh 터널링을 local pc 에서 실행
- ec2 는 사전에 생성되어 있어야한다.
- tlsAllowInvalidHostnames 옵션은 true 로 설정해서 붙어야 한다.
```
ssh -i "<your>.pem" -L 27017:<cluster-endpoint>:27017 ec2-user@<ec2-public-ip>
```

- mongosh 
```
mongosh --tls --host localhost:27017 --tlsCAFile global-bundle.pem --username <username> --password <password> --tlsAllowInvalidHostnames
```

- spring boot 에서 ssh 터널링을 통해서 connection 을 맺을 경우 아래 옵션은 필수로 필요하다.
- 클러스터 멤버 탐색을 비활성화하고, 명시된 호스트(localhost:27017)에 직접 연결.
```
directConnection=true
```