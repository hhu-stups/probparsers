# this is just a simple reminder on what kind of gradle tasks should be run
# deploy is the target to build the parsers and doesn't seem to do any deployment
# there does not seem to be a way to just build the probcliparser.jar

uberjar:
	echo "Building Uberjar"
	./gradlew clean uberjar
	echo "Now run make install to copy the jar to prob_prolog/lib"
install:
	cp build/libs/probcliparser-*.jar ../../prob_prolog/lib/probcliparser.jar
deploy:
	echo "Building ProB parsers"
	./gradlew deploy
clean:
	echo "Cleaning; useful if you encounter weird syntax errors during building"
	gradle clean

rebuild:
	echo "Building Uberjar without re-generating SableCC sources"
	echo "(allows to experiment with changes in SableCC source)"
	./gradlew --exclude-task :bparser:generateSableCCSource uberjar