#!/bin/bash -i
set -e

##################################################
### SET THE PATH TO YOUR ANDROID NDK DIRECTORY ###
##################################################
NDK_ROOT=${HOME}/android-ndk
##################################################

NDK_BUILD=${NDK_ROOT}/ndk-build
JNI_PATH=`pwd`
CC=`${NDK_ROOT}/ndk-which gcc`
LIBDIR=${JNI_PATH}/../obj/local/armeabi
JAVA_OUTPUT_PATH=${JNI_PATH}/../java
APP_PLATFORM=`grep APP_PLATFORM Application.mk | cut -d '=' -f 2`
LOG_FILE=/dev/null

CRYPTOPP=cryptopp
CRYPTOPP_VERSION=563
CRYPTOPP_SOURCE_FILE=cryptopp${CRYPTOPP_VERSION}.zip
CRYPTOPP_SOURCE_FOLDER=${CRYPTOPP}/${CRYPTOPP}
CRYPTOPP_DOWNLOAD_URL=http://www.cryptopp.com/${CRYPTOPP_SOURCE_FILE}
CRYPTOPP_SHA1="f2fcd1fbf884bed70a69b565970ecd8b33a68cc4"

SQLITE=sqlite
SQLITE_VERSION=3120200
SQLITE_YEAR=2016
SQLITE_BASE_NAME=sqlite-amalgamation-${SQLITE_VERSION}
SQLITE_SOURCE_FILE=${SQLITE_BASE_NAME}.zip
SQLITE_SOURCE_FOLDER=${SQLITE}/${SQLITE}
SQLITE_DOWNLOAD_URL=http://www.sqlite.org/${SQLITE_YEAR}/${SQLITE_SOURCE_FILE}
SQLITE_SHA1="22632bf0cfacedbeddde9f92695f71cab8d8c0a5"

CURL=curl
CURL_VERSION=7.48.0
C_ARES_VERSION=1.15.0
CURL_EXTRA="--disable-smb --disable-ftp --disable-file --disable-ldap --disable-ldaps --disable-rtsp --disable-proxy --disable-dict --disable-telnet --disable-tftp --disable-pop3 --disable-imap --disable-smtp --disable-gopher --disable-sspi"
CURL_SOURCE_FILE=curl-${CURL_VERSION}.tar.gz
CURL_SOURCE_FOLDER=curl-${CURL_VERSION}
CURL_DOWNLOAD_URL=http://curl.haxx.se/download/${CURL_SOURCE_FILE}
CURL_SHA1="eac95625b849408362cf6edb0bc9489da317ba30"

ARES_SOURCE_FILE=c-ares-${C_ARES_VERSION}.tar.gz
ARES_SOURCE_FOLDER=c-ares-${C_ARES_VERSION}
ARES_CONFIGURED=${CURL}/${ARES_SOURCE_FOLDER}/Makefile.inc
ARES_DOWNLOAD_URL=http://c-ares.haxx.se/download/${ARES_SOURCE_FILE}
ARES_SHA1="74a50c02b7f051c4fb66c0f60f187350f196d908"

OPENSSL=openssl
OPENSSL_VERSION=1.0.2h
OPENSSL_SOURCE_FILE=openssl-${OPENSSL_VERSION}.tar.gz
OPENSSL_SOURCE_FOLDER=${OPENSSL}-${OPENSSL_VERSION}
OPENSSL_DOWNLOAD_URL=http://www.openssl.org/source/${OPENSSL_SOURCE_FILE}
OPENSSL_PREFIX=${JNI_PATH}/${OPENSSL}/${OPENSSL_SOURCE_FOLDER}
OPENSSL_SHA1="577585f5f5d299c44dd3c993d3c0ac7a219e4949"

SODIUM=sodium
SODIUM_VERSION=1.0.16
SODIUM_SOURCE_FILE=libsodium-${SODIUM_VERSION}.tar.gz
SODIUM_SOURCE_FOLDER=libsodium-${SODIUM_VERSION}
SODIUM_DOWNLOAD_URL=https://download.libsodium.org/libsodium/releases/${SODIUM_SOURCE_FILE}
SODIUM_SHA1="c7ea321d7b8534e51c5e3d86055f6c1aa1e48ee9"

LIBUV=libuv
LIBUV_VERSION=1.8.0
LIBUV_SOURCE_FILE=libuv-v${LIBUV_VERSION}.tar.gz
LIBUV_SOURCE_FOLDER=libuv-v${LIBUV_VERSION}
LIBUV_DOWNLOAD_URL=http://dist.libuv.org/dist/v${LIBUV_VERSION}/${LIBUV_SOURCE_FILE}
LIBUV_SHA1="91ea51844ec0fac1c6358a7ad3e8bba128e9d0cc"

MEDIAINFO=mediainfo
MEDIAINFO_VERSION=4ee7f77c087b29055f48d539cd679de8de6f9c48
MEDIAINFO_SOURCE_FILE=${MEDIAINFO_VERSION}.zip
MEDIAINFO_SOURCE_FOLDER=MediaInfoLib-${MEDIAINFO_VERSION}
MEDIAINFO_DOWNLOAD_URL=https://github.com/meganz/MediaInfoLib/archive/${MEDIAINFO_SOURCE_FILE}
MEDIAINFO_SHA1="30927c761418e807d8d3b64e171a6c9ab9659c2e"

ZENLIB=ZenLib
ZENLIB_VERSION=6694a744d82d942c4a410f25f916561270381889
ZENLIB_SOURCE_FILE=${ZENLIB_VERSION}.zip
ZENLIB_SOURCE_FOLDER=ZenLib-${ZENLIB_VERSION}
ZENLIB_DOWNLOAD_URL=https://github.com/MediaArea/ZenLib/archive/${ZENLIB_SOURCE_FILE}
ZENLIB_SHA1="1af04654c9618f54ece624a0bad881a3cfef3692"

LIBWEBSOCKETS=libwebsockets
LIBWEBSOCKETS_VERSION=91de9a4a69b9b4af38b662c8bd9adbd1b4370ae0
LIBWEBSOCKETS_SOURCE_FILE=libwebsockets-${LIBWEBSOCKETS_VERSION}.zip
LIBWEBSOCKETS_SOURCE_FOLDER=libwebsockets-${LIBWEBSOCKETS_VERSION}
LIBWEBSOCKETS_DOWNLOAD_URL=https://github.com/warmcat/libwebsockets/archive/${LIBWEBSOCKETS_VERSION}.zip
LIBWEBSOCKETS_SHA1="ec7b329dfa37452d08d873afd5aaec4ac61e16db"

PDFVIEWER=pdfviewer
PDFVIEWER_VERSION=1.8.2
PDFVIEWER_SOURCE_FILE=PdfiumAndroid-pdfium-android-${PDFVIEWER_VERSION}.zip
PDFVIEWER_SOURCE_FOLDER=PdfiumAndroid-pdfium-android-${PDFVIEWER_VERSION}
PDFVIEWER_DOWNLOAD_URL=https://github.com/barteksc/PdfiumAndroid/archive/pdfium-android-${PDFVIEWER_VERSION}.zip
PDFVIEWER_SHA1="93205f9cff143d864c138534f3205351e3d6c42e"

function downloadCheckAndUnpack()
{
    local URL=$1
    local FILENAME=$2
    local SHA1=$3
    local TARGETPATH=$4
    
    if [[ -f ${FILENAME} ]]; then
        echo "* Already downloaded: '${FILENAME}'"
        local CURRENTSHA1=`sha1sum ${FILENAME} | cut -d " " -f 1`
        if [ "${SHA1}" != "${CURRENTSHA1}" ]; then
            echo "* Invalid hash. Redownloading..."
            wget -O ${FILENAME} ${URL} &>> ${LOG_FILE}
        fi
    else
        echo "* Downloading '${FILENAME}' ..."
        wget -O ${FILENAME} ${URL} &>> ${LOG_FILE}
    fi

    local NEWSHA1=`sha1sum ${FILENAME} | cut -d " " -f 1`
    if [ "${SHA1}" != "${NEWSHA1}" ]; then
        echo "* Invalid hash. It is ${NEWSHA1} but it should be ${SHA1}. Aborting..."
        exit 1
    fi

    if [[ "${FILENAME}" =~ \.tar\.[^\.]+$ ]]; then
        echo "* Extracting TAR file..."
        tar --overwrite -xf ${FILENAME} -C ${TARGETPATH} &>> ${LOG_FILE}
    elif [[ "${FILENAME}" =~ \.zip$ ]]; then
        echo "* Extracting ZIP file..."
    	unzip -o ${FILENAME} -d ${TARGETPATH} &>> ${LOG_FILE}
    else
        echo "* Dont know how to extract '${FILENAME}'"
        exit 1
    fi

    echo "* Extraction finished"
}

function createMEGABindings
{
    echo "* Creating MEGA Java bindings"
    mkdir -p ../java/nz/mega/sdk
    swig -c++ -Imega/sdk/include -java -package nz.mega.sdk -outdir ${JAVA_OUTPUT_PATH}/nz/mega/sdk -o bindings/megasdk.cpp -DHAVE_LIBUV -DENABLE_CHAT mega/sdk/bindings/megaapi.i &>> ${LOG_FILE}
}

function createMEGAchatBindings
{
    echo "* Creating MEGAchat Java bindings"
    mkdir -p ../java/nz/mega/sdk
    swig -c++ -Imega/sdk/include -Imegachat/sdk/src/ -java -package nz.mega.sdk -outdir ${JAVA_OUTPUT_PATH}/nz/mega/sdk/ -o bindings/megachat.cpp megachat/sdk/bindings/megachatapi.i &>> ${LOG_FILE}
    pushd megachat/sdk/src &>> ${LOG_FILE}
    cmake -P genDbSchema.cmake
    popd &>> ${LOG_FILE}
}

if [ ! -d "${NDK_ROOT}" ]; then
    echo "* NDK_ROOT not set. Please edit this file to configure it correctly and try again."    
    exit 1
fi

if (( $# != 1 )); then
    echo "Usage: $0 <all | bindings | clean | clean_mega>";
    exit 0 
fi

if [ "$1" == "bindings" ]; then
    createMEGAchatBindings
    createMEGABindings
    echo "* Bindings ready!"
    echo "* Running ndk-build"
    ${NDK_BUILD} -j8
    echo "* ndk-build finished"
    echo "* Task finished OK"
    exit 0
fi

if [ "$1" == "clean_mega" ]; then
    echo "* Deleting Java bindings"
    make -C mega -f MakefileBindings clean JAVA_BASE_OUTPUT_PATH=${JAVA_OUTPUT_PATH} &>> ${LOG_FILE}
    rm -rf megachat/megachat.cpp megachat/megachat.h
    echo "* Deleting tarballs"
    rm -rf ../obj/local/armeabi
    rm -rf ../obj/local/x86
    echo "* Task finished OK"
    exit 0
fi

if [ "$1" == "clean" ]; then
    echo "* Deleting Java bindings"
    make -C mega -f MakefileBindings clean JAVA_BASE_OUTPUT_PATH=${JAVA_OUTPUT_PATH} &>> ${LOG_FILE}
    rm -rf megachat/megachat.cpp megachat/megachat.h
    
    echo "* Deleting source folders"    
    rm -rf ${CRYPTOPP_SOURCE_FOLDER}
    rm -rf ${SQLITE_SOURCE_FOLDER} ${SQLITE}/${SQLITE_BASE_NAME}
    rm -rf ${CURL}/${CURL_SOURCE_FOLDER}
    rm -rf ${CURL}/${CURL}
    rm -rf ${CURL}/${ARES_SOURCE_FOLDER}
    rm -rf ${CURL}/ares
    rm -rf ${OPENSSL}/${OPENSSL_SOURCE_FOLDER}
    rm -rf ${OPENSSL}/${OPENSSL}
    rm -rf ${SODIUM}/${SODIUM_SOURCE_FOLDER}
    rm -rf ${SODIUM}/${SODIUM}
    rm -rf ${LIBUV}/${LIBUV_SOURCE_FOLDER}
    rm -rf ${LIBUV}/${LIBUV}
    rm -rf ${MEDIAINFO}/${ZENLIB_SOURCE_FOLDER}
    rm -rf ${MEDIAINFO}/${ZENLIB}
    rm -rf ${MEDIAINFO}/${MEDIAINFO_SOURCE_FOLDER}
    rm -rf ${MEDIAINFO}/${MEDIAINFO}
    rm -rf ${LIBWEBSOCKETS}/${LIBWEBSOCKETS_SOURCE_FOLDER}
    rm -rf ${LIBWEBSOCKETS}/${LIBWEBSOCKETS}
    rm -rf ${PDFVIEWER}/${PDFVIEWER}

    echo "* Deleting tarballs"
    rm -rf ${CRYPTOPP}/${CRYPTOPP_SOURCE_FILE}
    rm -rf ${CRYPTOPP}/${CRYPTOPP_SOURCE_FILE}.ready
    rm -rf ${SQLITE}/${SQLITE_SOURCE_FILE}
    rm -rf ${SQLITE}/${SQLITE_SOURCE_FILE}.ready
    rm -rf ${CURL}/${CURL_SOURCE_FILE}
    rm -rf ${CURL}/${ARES_SOURCE_FILE}
    rm -rf ${CURL}/${CURL_SOURCE_FILE}.ready
    rm -rf ${OPENSSL}/${OPENSSL_SOURCE_FILE}
    rm -rf ${OPENSSL}/${OPENSSL_SOURCE_FILE}.ready
    rm -rf ${SODIUM}/${SODIUM_SOURCE_FILE}
    rm -rf ${SODIUM}/${SODIUM_SOURCE_FILE}.ready
    rm -rf ${LIBUV}/${LIBUV_SOURCE_FILE}
    rm -rf ${LIBUV}/${LIBUV_SOURCE_FILE}.ready
    rm -rf ${MEDIAINFO}/${ZENLIB_SOURCE_FILE}
    rm -rf ${MEDIAINFO}/${ZENLIB_SOURCE_FILE}.ready
    rm -rf ${MEDIAINFO}/${MEDIAINFO_SOURCE_FILE}
    rm -rf ${MEDIAINFO}/${MEDIAINFO_SOURCE_FILE}.ready
    rm -rf ${LIBWEBSOCKETS}/${LIBWEBSOCKETS_SOURCE_FILE}
    rm -rf ${LIBWEBSOCKETS}/${LIBWEBSOCKETS_SOURCE_FILE}.ready

    echo "* Deleting object files"
    rm -rf ../obj/local/armeabi-v7a/	
    rm -rf ../obj/local/armeabi/
    rm -rf ../obj/local/x86
    
    echo "* Deleting libraries"
    rm -rf ../libs/armeabi-v7a
    rm -rf ../libs/armeabi
    rm -rf ../libs/x86

    rm -rf ${PDFVIEWER}/${PDFVIEWER_SOURCE_FILE}
    rm -rf ${PDFVIEWER}/${PDFVIEWER_SOURCE_FILE}.ready
    rm -rf ../obj/local/armeabi/
    rm -rf ../obj/local/x86
    echo "* Task finished OK"
    exit 0
fi

if [ "$1" != "all" ]; then
    echo "Usage: $0 <all | bindings | clean | clean_mega>";
    exit 1
fi

createMEGAchatBindings
echo "* MEGAchat is ready"

echo "* Setting up MEGA"
createMEGABindings
echo "* MEGA is ready"

echo "* Setting up libsodium"
if [ ! -f ${SODIUM}/${SODIUM_SOURCE_FILE}.ready ]; then
    downloadCheckAndUnpack ${SODIUM_DOWNLOAD_URL} ${SODIUM}/${SODIUM_SOURCE_FILE} ${SODIUM_SHA1} ${SODIUM}
    ln -sf ${SODIUM_SOURCE_FOLDER} ${SODIUM}/${SODIUM}
    pushd ${SODIUM}/${SODIUM} &>> ${LOG_FILE}
    export ANDROID_NDK_HOME=${NDK_ROOT}
    ./autogen.sh &>> ${LOG_FILE}
    echo "#include <limits.h>" >>  src/libsodium/include/sodium/export.h
    sed -i 's/enable-minimal/enable-minimal --disable-pie/g' dist-build/android-build.sh
    echo "* Prebuilding libsodium for ARM"
    dist-build/android-arm.sh &>> ${LOG_FILE}
    echo "* Prebuilding libsodium for ARMv7"
    dist-build/android-armv7-a.sh &>> ${LOG_FILE}
    echo "* Prebuilding libsodium for x86"
    dist-build/android-x86.sh &>> ${LOG_FILE}
    ln -sf libsodium-android-armv6 libsodium-android-armeabi
    ln -sf libsodium-android-armv6 libsodium-android-armeabi-v7
    ln -sf libsodium-android-armv7-a libsodium-android-armeabi-v7a
    ln -sf libsodium-android-i686 libsodium-android-x86
    popd &>> ${LOG_FILE}
    touch ${SODIUM}/${SODIUM_SOURCE_FILE}.ready
fi
echo "* libsodium is ready"

echo "* Setting up Crypto++"
if [ ! -f ${CRYPTOPP}/${CRYPTOPP_SOURCE_FILE}.ready ]; then
    mkdir -p ${CRYPTOPP}/${CRYPTOPP}
    downloadCheckAndUnpack ${CRYPTOPP_DOWNLOAD_URL} ${CRYPTOPP}/${CRYPTOPP_SOURCE_FILE} ${CRYPTOPP_SHA1} ${CRYPTOPP}/${CRYPTOPP}
    touch ${CRYPTOPP}/${CRYPTOPP_SOURCE_FILE}.ready
fi
echo "* Crypto++ is ready"

echo "* Setting up SQLite"
if [ ! -f ${SQLITE}/${SQLITE_SOURCE_FILE}.ready ]; then
    downloadCheckAndUnpack ${SQLITE_DOWNLOAD_URL} ${SQLITE}/${SQLITE_SOURCE_FILE} ${SQLITE_SHA1} ${SQLITE}
    ln -fs ${SQLITE_BASE_NAME} ${SQLITE_SOURCE_FOLDER}
    touch ${SQLITE}/${SQLITE_SOURCE_FILE}.ready
fi
echo "* SQLite is ready"

echo "* Setting up libuv"
if [ ! -f ${LIBUV}/${LIBUV_SOURCE_FILE}.ready ]; then
    downloadCheckAndUnpack ${LIBUV_DOWNLOAD_URL} ${LIBUV}/${LIBUV_SOURCE_FILE} ${LIBUV_SHA1} ${LIBUV}
    ln -sf ${LIBUV_SOURCE_FOLDER} ${LIBUV}/${LIBUV}
    touch ${LIBUV}/${LIBUV_SOURCE_FILE}.ready
fi
echo "* libuv is ready"

echo "* Setting up ZenLib"
if [ ! -f ${MEDIAINFO}/${ZENLIB_SOURCE_FILE}.ready ]; then
    downloadCheckAndUnpack ${ZENLIB_DOWNLOAD_URL} ${MEDIAINFO}/${ZENLIB_SOURCE_FILE} ${ZENLIB_SHA1} ${MEDIAINFO}
    ln -sf ${ZENLIB_SOURCE_FOLDER} ${MEDIAINFO}/${ZENLIB}
    cp mega/sdk/include/mega/mega_glob.h ${MEDIAINFO}/${ZENLIB}/Source/ZenLib/glob.h
    touch ${MEDIAINFO}/${ZENLIB_SOURCE_FILE}.ready
fi
echo "* ZenLib is ready"

echo "* Setting up MediaInfo"
if [ ! -f ${MEDIAINFO}/${MEDIAINFO_SOURCE_FILE}.ready ]; then
    downloadCheckAndUnpack ${MEDIAINFO_DOWNLOAD_URL} ${MEDIAINFO}/${MEDIAINFO_SOURCE_FILE} ${MEDIAINFO_SHA1} ${MEDIAINFO}
    ln -sf ${MEDIAINFO_SOURCE_FOLDER} ${MEDIAINFO}/${MEDIAINFO}
    touch ${MEDIAINFO}/${MEDIAINFO_SOURCE_FILE}.ready
fi
echo "* MediaInfo is ready"

echo "* Setting up OpenSSL"
if [ ! -f ${OPENSSL}/${OPENSSL_SOURCE_FILE}.ready ]; then
    downloadCheckAndUnpack ${OPENSSL_DOWNLOAD_URL} ${OPENSSL}/${OPENSSL_SOURCE_FILE} ${OPENSSL_SHA1} ${OPENSSL}
    ln -sf ${OPENSSL_SOURCE_FOLDER} ${OPENSSL}/${OPENSSL}
    ln -sf ${LIBDIR} ${OPENSSL}/${OPENSSL_SOURCE_FOLDER}/lib
    pushd ${OPENSSL}/${OPENSSL} &>> ${LOG_FILE}
    ./Configure android &>> ${LOG_FILE}
    popd &>> ${LOG_FILE}
    touch ${OPENSSL}/${OPENSSL_SOURCE_FILE}.ready
fi
echo "* OpenSSL is ready"

echo "* Setting up cURL with c-ares"
if [ ! -f ${CURL}/${CURL_SOURCE_FILE}.ready ]; then
    echo "* Setting up cURL"
    downloadCheckAndUnpack ${CURL_DOWNLOAD_URL} ${CURL}/${CURL_SOURCE_FILE} ${CURL_SHA1} ${CURL}
    ln -sf ${CURL_SOURCE_FOLDER} ${CURL}/${CURL}
    echo "* cURL is ready"

    echo "* Setting up c-ares"
    downloadCheckAndUnpack ${ARES_DOWNLOAD_URL} ${CURL}/${ARES_SOURCE_FILE} ${ARES_SHA1} ${CURL}
    ln -sf ${ARES_SOURCE_FOLDER} ${CURL}/ares
    ln -sf ../${ARES_SOURCE_FOLDER} ${CURL}/${CURL_SOURCE_FOLDER}/ares
    echo "* c-ares is ready"
    touch ${CURL}/${CURL_SOURCE_FILE}.ready
fi
echo "* cURL with c-ares is ready"

echo "* Setting up libwebsockets"
if [ ! -f ${LIBWEBSOCKETS}/${LIBWEBSOCKETS_SOURCE_FILE}.ready ]; then
    downloadCheckAndUnpack ${LIBWEBSOCKETS_DOWNLOAD_URL} ${LIBWEBSOCKETS}/${LIBWEBSOCKETS_SOURCE_FILE} ${LIBWEBSOCKETS_SHA1} ${LIBWEBSOCKETS}
    ln -sf ${LIBWEBSOCKETS_SOURCE_FOLDER} ${LIBWEBSOCKETS}/${LIBWEBSOCKETS}
    touch ${LIBWEBSOCKETS}/${LIBWEBSOCKETS_SOURCE_FILE}.ready
fi
echo "* libwebsockets is ready"


echo "* Checking WebRTC"
if grep ^DISABLE_WEBRTC Application.mk | grep --quiet false; then
    if [ ! -d megachat/webrtc/include ]; then
        echo "ERROR: WebRTC not ready. Please download it from this link: https://mega.nz/#!lxNFnYqA!A7zXNm0JBCSVgowjIrFTkRUwj0zbNHJ37iHXF58rzc4"
        echo "and uncompress it in megachat/webrtc"
        exit 1
    else
        echo "* WebRTC is ready"
    fi
else
    echo "* WebRTC is not needed"
fi

echo "* Setting up PdfViewer"
if [ ! -f ${PDFVIEWER}/${PDFVIEWER_SOURCE_FILE}.ready ]; then
    downloadCheckAndUnpack ${PDFVIEWER_DOWNLOAD_URL} ${PDFVIEWER}/${PDFVIEWER_SOURCE_FILE} ${PDFVIEWER_SHA1} ${PDFVIEWER}
    cd ${PDFVIEWER}
    rm -rf ${PDFVIEWER}
    mkdir ${PDFVIEWER}
    cd ${PDFVIEWER}
    mkdir include
    mkdir lib
    mkdir src
    cp -R ../${PDFVIEWER_SOURCE_FOLDER}/src/main/jni/include/* ./include/
    cp -R ../${PDFVIEWER_SOURCE_FOLDER}/src/main/jni/lib/* ./lib/
    cp -R ../${PDFVIEWER_SOURCE_FOLDER}/src/main/jni/src/* ./src/
    rm -rf ../${PDFVIEWER_SOURCE_FOLDER}
    cd ../..
    touch ${PDFVIEWER}/${PDFVIEWER_SOURCE_FILE}.ready
fi
echo "* PdfViewer is ready"

echo "* All dependencies are prepared!"
echo "* Running ndk-build"
${NDK_BUILD} -j8
echo "* ndk-build finished"
echo "* Task finished OK"

