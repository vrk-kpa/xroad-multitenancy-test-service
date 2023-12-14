self_signed_cert () {
  openssl req \
    -x509 \
    -newkey rsa:4096 \
    -keyout "$1/certs/privatekey.pem" \
    -out "$1/certs/cert.pem" \
    -sha256 \
    -days 3650 \
    -nodes \
    -subj "/C=FI/ST=/L=Helsinki/O=$1/OU=SomeUnit/CN=$1.com"

  chmod 0700 "$1/certs/privatekey.pem"
}

self_signed_cert org1
self_signed_cert org2
self_signed_cert org3
