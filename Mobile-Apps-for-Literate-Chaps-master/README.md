# Mobile-Apps-for-Literate-Chaps
ARDICT

IDE : Android Studio

APIS : Merriam-Webster Dictionary, Oxford Dictionary, Urban Dictionary

3rd Party libraries : TensorFlow, OpenCV, ARCore

TensorFlow : https://www.tensorflow.org/install/lang_java

OpenCV : https://opencv-java-tutorials.readthedocs.io/en/latest/01-installing-opencv-for-java.html

ARCore : https://developers.google.com/ar/develop/java/quickstart

How to build ARDICT:
(Currently no 3rd party libraries included)
- Clone the repository
- On Windows run:
	gradlew build
- On Mac run:
	./gradlew build
- Alternatively click the green run button in Android studio


Folder and File Structure: 
- All gradle files are used to build and run our project
- The 'app/src' folder will contain all our code plus our tests
- In this directory, the 'main' folder will contain both the UI (view) and the majority of the code (controller/model)
.

├── app  
│   └── src  
│       ├── androidTest  
│       ├── main  
│       └── test  
└── gradle  
	└── wrapper  
