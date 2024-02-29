cd ..
./gradlew clean distZip
cd .local

docker build -f Dockerfile ..  -t rinhabackend-2401:latest

tag=$(date +'%Y%m%d%M%S')
docker tag rinhabackend-2401 cleidianosouza/rinha-backend-2024-q1:$tag
