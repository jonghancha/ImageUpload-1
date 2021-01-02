# ImageUpload
Gallery --> Activity --> Server

- Copyright : 고종찬, 김대환, 김태현, 차종한

- Mutipart JSP Folder : Tomcat >> webapps >> ROOT >> jsp
- Upload File Folder : Tomcat >> webapps >> ROOT >> images
- Upload Library :  Tomcat >> lib <- cos.jar

Android Studio Open후
- Android >> Gradle Scripts >> build.gradle(Module)에 okhttp3 추가

- Image Upload방식을 AsyncTask 사용
- AsyncTask의 doInBackground Method의 결과값으로 Upload성공여부 확인
- Device에서 파일명 지정하기 위한 임시파일 삭제
