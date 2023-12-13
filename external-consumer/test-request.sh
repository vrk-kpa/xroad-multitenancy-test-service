ORG="${1:-org1}"
ENDPOINT="${2:-random}"

echo "requesting $ENDPOINT as $ORG"

curl \
  --cert "$ORG/certs/cert.pem"\
  --key "$ORG/certs/privatekey.pem" \
  --cacert "trusted-certs/test-client-cert.pem" \
  "https://xroad-multitenancy-test-client:8443/rest-api/$ENDPOINT"
