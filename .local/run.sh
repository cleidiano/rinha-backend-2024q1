./build-image.sh
docker-compose down
docker-compose up -d --force-recreate

echo "waiting...."
sleep 10;

cd `pwd`/../../rinha-de-backend-2024-q1
./executar-teste-local.sh