#!/bin/sh

./gradlew tokenTest --args='-f token_test.txt -p patterns.txt -rp -cf'
## java -cp classes ir.TokenTest -f token_test.txt -p patterns.txt -rp -cf > tokenized_result.txt

printf "Displaying diff result between %s and %s:\n" "tokenized_result.txt" "token_test_tokenized_ok.txt"
## unix diff
diff tokenized_result.txt token_test_tokenized_ok.txt --strip-trailing-cr
