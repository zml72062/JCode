
all: Main

run: all
	java Main
	
Main:
	rm -f *.class file/*.class gui/*.class shell/*.class
	javac Main.java

.PHONY: clean
clean:
	rm -f *.class file/*.class gui/*.class shell/*.class

.PHONY: cntlines
cntlines:
	find . -name '*.java' | xargs wc -l 
