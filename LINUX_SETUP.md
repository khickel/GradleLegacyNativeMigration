docker run -it --rm --mount type=bind,source="$(pwd)",target=/app ubuntu:20.04 bash
apt update
apt install openjdk-11-jdk-headless git curl zip unzip tar gcc g++ build-essential pkg-config
cd opt
git clone https://github.com/microsoft/vcpkg
./vcpkg/bootstrap-vcpkg.sh
./vcpkg/vcpkg install openssl
cd /app
./gradlew assemble

