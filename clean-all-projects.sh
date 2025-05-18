find . -name ".terraform" -exec rm -r .terraform '{}' \;

cd microservices/Purchase	
mvn clean
cd ../..

cd microservices/customer	
mvn clean
cd ../..

cd microservices/loyaltycard	
mvn clean
cd ../..

cd microservices/shop
mvn clean
cd ../..

cd microservices/discountcoupon
mvn clean
cd ../..

cd microservices/selledproduct
mvn clean
cd ../..

cd microservices/csrecommendation
mvn clean
cd ../..

cd microservices/couponanalysis
mvn clean
cd ../..