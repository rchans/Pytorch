Secure Socket Support
=====================

By default, the server and client will use plain TCP sockets to communicate.
By configuring a keystore for the server and a truststore for the client,
the communication can be switched to secure (TLS) sockets.
The sockets are encrypted, and clients will only communicate with trusted servers.

Step 1: Create a server KEYSTORE that contains a public and private key.
-------

The server passes the public key to clients. Clients then use that to encrypt messages
to the server which only the server can decode with its private key.

```
keytool -genkey -alias mykey -dname "CN=myself" -keystore KEYSTORE -storepass changeit -keyalg RSA
```

To check, note "Entry type: PrivateKeyEntry" because the certificate holds both a public and private key:

```
keytool -list -v -keystore KEYSTORE -storepass changeit
```

This example so far only uses self-signed certificates.
An operational setup might prefer to sign them by a publicly trusted certificate authority.


Step 2: Create a client TRUSTSTORE to register the public server key
-------

Clients check a list of public keys to identify trusted servers.
Clients can technically use the keystore we just created, but
they should really only have access to the server's public key,
not the server's private key.
In addition, you may want to add public keys from more than one server into
the client truststore.

First export the server's public key.

```
keytool -export -alias mykey -keystore KEYSTORE -storepass changeit -rfc -file mykey.cer
```

Import the certificate into a new client truststore.

```
keytool -import -alias mykey -file mykey.cer -keystore TRUSTSTORE -storepass changeit -noprompt
```

To check, note "Entry type: trustedCertEntry" because the truststore contains only public keys:

```
keytool -list -v -keystore TRUSTSTORE -storepass changeit
```

While the key and trust files were created with the Java keytool,
they are compatible with generic open SSL tools:

```
openssl pkcs12 -info -in KEYSTORE -nodes
```


Step 3: Configure and run the demo server
-------

Set environment variables to inform the server about its keystore:

```
export EPICS_PVAS_TLS_KEYCHAIN=/path/to/KEYSTORE
export EPICS_PVA_STOREPASS=changeit
```

Then run a demo server

```
java -cp target/classes org/epics/pva/server/ServerDemo
```


Step 4: Configure and run the demo client
-------

Set environment variables to inform the client about its truststore:

```
export EPICS_PVA_TLS_KEYCHAIN=/path/to/TRUSTSTORE
export EPICS_PVA_STOREPASS=changeit
```

Then run a demo client

```
java -cp target/classes org/epics/pva/client/PVAClientMain get demo
```


More
----

Add `-Djavax.net.debug=all` to see encryption information.

For example, when receiving subscription updates for a string PV with status and timestamp,
the log messages indicate that the encrypted data size of 74 bytes is almost twice the size
of the decrypted payload of 41 bytes: 

```
javax.net.ssl|DEBUG|91|TCP receiver /127.0.0.1|2023-05-05 15:57:37.299 EDT|SSLSocketInputRecord.java:214|READ: TLSv1.2 application_data, length = 74
javax.net.ssl|DEBUG|91|TCP receiver /127.0.0.1|2023-05-05 15:57:37.299 EDT|SSLSocketInputRecord.java:493|Raw read (
  0000: 65 27 79 1D 33 5D 7C AB   A6 06 86 31 0D 9C 10 70  e'y.3].....1...p
  0010: 3C 85 58 D3 FD AA 81 57   00 0A 04 DF 37 3D EE 31  <.X....W....7=.1
  0020: C4 2A CE E5 24 A3 E8 F3   F2 B6 7E 64 BE 32 9E 71  .*..$......d.2.q
  0030: F8 29 81 C4 3F 61 D0 E4   D1 D5 A7 BC 5A 21 D0 B8  .)..?a......Z!..
  0040: F5 5F DD 5E DE 59 79 F8   45 86                    ._.^.Yy.E.
)
javax.net.ssl|DEBUG|91|TCP receiver /127.0.0.1|2023-05-05 15:57:37.300 EDT|SSLCipher.java:1935|Plaintext after DECRYPTION (
  0000: CA 02 40 0D 21 00 00 00   01 00 00 00 00 02 02 03  ..@.!...........
  0010: 0B 56 61 6C 75 65 20 69   73 20 38 33 B1 5F 55 64  .Value is 83._Ud
  0020: 00 00 00 00 10 0E AD 11   00                       .........
```

Firewalls
---------

To allow tests from other hosts, may need to open firewalls.
For RHEL9, use this get both immediate openings and have them
persist a reboot:

```
# Default UDP search port
sudo firewall-cmd --zone=public --add-port=5076/udp
sudo firewall-cmd --zone=public --add-port=5076/udp --permanent
# Default plain TCP port
sudo firewall-cmd --zone=public --add-port=5075/tcp
sudo firewall-cmd --zone=public --add-port=5075/tcp --permanent
# Default secure (TLS) TCP port
sudo firewall-cmd --zone=public --add-port=5076/tcp
sudo firewall-cmd --zone=public --add-port=5076/tcp --permanent

# Show
sudo firewall-cmd --info-zone=public
```

Use a Certification Authority
-----------------------------

Instead of creating a separate key pair for each server and telling the client to trust all those public keys,
we can use a Certification Authority.
That way, clients trust the CA, and you can create individual key pairs for servers without need
to distribute their public keys to each client.

For a standalone demo, we create our own CA, and make its public certificate available as `myca.cer`:

```
keytool -genkeypair -alias myca -keystore ca.p12 -storepass changeit -dname "CN=myca" -keyalg RSA -ext bc=ca:true
keytool -list                   -keystore ca.p12 -storepass changeit
keytool -exportcert -alias myca -keystore ca.p12 -storepass changeit -rfc -file myca.cer
keytool -printcert -file myca.cer 
```

Now create a server keypair for use by the IOC:

```
keytool -genkeypair -alias myioc -keystore ioc.p12 -storepass changeit -dname "CN=myioc" -keyalg RSA
keytool -list -v                 -keystore ioc.p12 -storepass changeit
```

It starts out as a "self-signed certificate" with matching owner and issuer.
Create a certificate signing request. The CSR could be sent to a commercial CA, but we sign it with our own CA.

```
keytool -certreq -alias myioc -keystore ioc.p12 -storepass changeit -file myioc.csr
keytool -gencert -alias myca  -keystore ca.p12  -storepass changeit -ext san=dns:myioc -infile myioc.csr -outfile myioc.cer
keytool -printcert -file myioc.cer
```

Import the signed certificate into the ioc keystore. Since `ioc.cer` is signed by 'myca', which
is not a generally known CA, we will get an error "Failed to establish chain"
unless we first import `myca.cer` to trust out local CA.

```
keytool -importcert -alias myca  -keystore ioc.p12 -storepass changeit -file myca.cer  -noprompt
keytool -importcert -alias myioc -keystore ioc.p12 -storepass changeit -file myioc.cer
keytool -list -v                 -keystore ioc.p12 -storepass changeit
```

A client will trust any IOC certificate signed by 'myca' once it's aware of the 'myca' certificate,
which needs to be imported into the PKCS12 file format:

```
keytool -importcert -alias myca  -keystore trust_ca.p12 -storepass changeit -file myca.cer  -noprompt
```

We can now run the server with `EPICS_PVAS_TLS_KEYCHAIN=/path/to/ioc.p12` and clients with
`EPICS_PVA_TLS_KEYCHAIN=/path/to/trust_ca.p12`, both with `EPICS_PVA_STOREPASS=changeit`

