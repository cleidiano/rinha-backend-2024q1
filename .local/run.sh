cd ..
./gradlew clean distZip
cd .local

docker build -f Dockerfile ..  -t rinhabackend-2401:latest
docker-compose down
docker-compose up -d --force-recreate

echo "waiting...."
sleep 10;

cd /Users/cleidiano.souza/projects/oss/rinha-de-backend-2024-q1
./executar-teste-local.sh