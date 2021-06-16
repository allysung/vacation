
## 정상 시나리오 
### 휴가 취소 :  로그인 API 호출 -> 휴가 목록 API 호출 -> 휴가 신청 API 호출 -> 휴가 상세 조회 API 호출 -> 휴가 취소 API 호출

1. 로그인 API 호출
  curl --location --request POST 'http://localhost:8080/oauth/token' \
  --header 'Content-Type: application/x-www-form-urlencoded' \
  --header 'Authorization: Basic bXlBcHA6cGFzcw==' \
  --data-urlencode 'username=admin@email.com' \
  --data-urlencode 'password=admin' \
  --data-urlencode 'grant_type=password'


2. 휴가 신청 상세 목록 조회
   curl --location --request GET 'http://localhost:8080/api/v1/annuals/details' \
   --header 'Content-Type: application/json' \
   --header 'Authorization: Bearer {{auth_token}}'
   
   
3. 휴가 신청 annualType(ANNUAL: 연차, HALF_ANNUAL: 반차, QUARTER_ANNUAL:반반차)
   auth_token 로그인시 반환된 토근 사용
      
   curl --location --request POST 'http://localhost:8080/api/v1/annuals' \
   --header 'Content-Type: application/json' \
   --header 'Authorization: Bearer {{auth_token}}' \
   --data-raw '{
   "annualType": "ANNUAL",
   "startDate": "2021-05-17",
   "endDate": "2021-05-18",
   "requestDays": 2,
   "comment": "여름휴가"
   }'


4. 휴가 상세 조회
   curl --location --request GET 'http://localhost:8080/api/v1/annuals/details/4' \
   --header 'Content-Type: application/json' \
   --header 'Authorization: Bearer {{auth_token}}'


5. 휴가 취소 신청
   curl --location --request PATCH 'http://localhost:8080/api/v1/annuals/details/4' \
   --header 'Content-Type: application/json' \
   --header 'Authorization: Bearer {{auth_token}}' \
   --data-raw '{
   "annualStatus": "CANCEL"
   }'


## 연차를 모두 사용한 경우 or 남은 연차보다 많이 휴가를 신청한 경우 - http 상태코드 400 반환
### 로그인 API 호출 -> 휴가 신청 API 호출 
1. 로그인 API 호출
   curl --location --request POST 'http://localhost:8080/oauth/token' \
   --header 'Content-Type: application/x-www-form-urlencoded' \
   --header 'Authorization: Basic bXlBcHA6cGFzcw==' \
   --data-urlencode 'username=admin@email.com' \
   --data-urlencode 'password=admin' \
   --data-urlencode 'grant_type=password'
   

2. 휴가 신청 startDate, endDate 를 지난달로 설정 후 요청
   annualType(ANNUAL: 연차, HALF_ANNUAL: 반차, QUARTER_ANNUAL:반반차)
   auth_token 로그인시 반환된 토근 사용

   curl --location --request POST 'http://localhost:8080/api/v1/annuals' \
   --header 'Content-Type: application/json' \
   --header 'Authorization: Bearer {{auth_token}}' \
   --data-raw '{
   "annualType": "ANNUAL",
   "startDate": "2021-05-01",
   "endDate": "2021-05-29",
   "requestDays": 30,
   "comment": "여름휴가"
   }'


## 시작한 연차를 취소하는 경우 - http 400 상태코드 반환
### 로그인 API 호출 -> 휴가 신청 API 호출 -> 휴가 취소 API 호출

1. 로그인 API 호출
   curl --location --request POST 'http://localhost:8080/oauth/token' \
   --header 'Content-Type: application/x-www-form-urlencoded' \
   --header 'Authorization: Basic bXlBcHA6cGFzcw==' \
   --data-urlencode 'username=admin@email.com' \
   --data-urlencode 'password=admin' \
   --data-urlencode 'grant_type=password'

2. 휴가 신청 startDate, endDate 를 지난달(2021-04-01 ~ 2021-04-02)로 설정후 요청
   annualType(ANNUAL: 연차, HALF_ANNUAL: 반차, QUARTER_ANNUAL:반반차)
   auth_token 로그인시 반환된 토근 사용

   curl --location --request POST 'http://localhost:8080/api/v1/annuals' \
   --header 'Content-Type: application/json' \
   --header 'Authorization: Bearer {{auth_token}}' \
   --data-raw '{
   "annualType": "ANNUAL",
   "startDate": "2021-04-01",
   "endDate": "2021-04-02",
   "requestDays": 2,
   "comment": "개인정비"
   }'

5. 휴가 취소 신청
   curl --location --request PATCH 'http://localhost:8080/api/v1/annuals/details/{{annualDetaiId}}' \
   --header 'Content-Type: application/json' \
   --header 'Authorization: Bearer {{auth_token}}' \
   --data-raw '{
   "annualStatus": "CANCEL"
   }'
