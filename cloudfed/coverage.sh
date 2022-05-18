# evaluate coverage by running all tests, even slow ones
mvn -DslowTests=true clean jacoco:prepare-agent test jacoco:report
cat target/site/jacoco/index.html | perl -pe 's/.*<tfoot>.*?ctr2">(.*?)<\/td>.*/\1/' | awk 1 > coverage.txt
echo Instruction coverage: $(cat coverage.txt)
echo Coverage report in target/site/jacoco/index.html
