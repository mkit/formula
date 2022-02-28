# Formula 1 - Proof of Concept

This project is a demonstrator of a platform allowing users to buy and trade F1X tokens as well as
shares of the drivers

# Pre-Requisites

OSX

In order to run this PoC you will need the following:

* JDK 1.8 installed on your machine and set as your main Java runtime environment. Best way to install the JDK1.8 is to use the ``homebrew`` installer.
* GIT tooling - most likely your machine has it already installed. If not google the ways how you can install ``Git`` on your machine.

# Usage

In order to run the PoC you need to build it.
1. Download the sourcecode to your local machine by running the following command:
   ``git clone {{ URL_TO_THE_REPO }}``
2. Navigate to the project main directory and run the following commands:
   ``./gradlew clean deployNodes``
   ``./gradlew publishToMavenLocal``
   ``./build/nodes/runnodes``
3. After that you should have 6 terminal tabs running. Each tab corresponds to a single Corda node. Very often not all tabs get open in the first run. This is a known issue of the Corda local execution. The workaround it is to close/terminate all the tabs and try again the last command.
   You should not continue to the next step until the 6 tabs (i.e. Corda nodes) are up and running.
4. Once you have Corda nodes running, navigate to the ``clients`` directory. 
5. In each separate terminal tab run the following commands:
   ``./gradlew runGov``
   ``./gradlew runHouse1``
   ``./gradlew runHouse2``
   ``./gradlew runPlayer1``
   ``./gradlew runPlayer2``
6. Those five command starts the backends each connected to a dedicated Corda node. Those commands do not terminate so make sure your terminal tabs are open.
7. Open your browser and you can type the following addresses to navigate to each of the Corda node's frontends:
   ``0.0.0.0:8010`` - Gov node
   ``0.0.0.0:8020`` - House 1 node
   ``0.0.0.0:8030`` - House 2 node
   ``0.0.0.0:8040`` - Player1 node
   ``0.0.0.0:8050`` - Player2 node
