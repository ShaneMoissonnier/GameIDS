<div align="center">
	<br>
	<br>
	<img src="assets/images/jungle.png" width="200" height="200">
	<h1>GameIDS</h1>
	<p>
	<p>🌴 Introduction to distributed systems game made with RabbitMQ/Swing</p>
	</p>
	<br>
</div>

## Showcase

## Architecture

## Instructions

1. Compile the source code with the following command:

    ```console
         mvn clean
         mvn compile
    ```

    > Do not forget to run RabbitMQ server before running the dispatcher and the client:

    ```console
         sudo rabbitmq-server
    ```

2. Launch the dispatcher with a area number as argument at startup with the following command:
    ```console
          mvn exec:java@launch-dispatcher -DnbAreas=<n>
    ```
   > If "nbAreas" is equal to zero in this case no areas are launched, then they must be launched manually with the following command:
   
   ```console
         mvn exec:java@launch-area-manager
   ```
3. Launch a client (player) with this command:
    ```console
         mvn exec:java@launch-player
    ```

## Potential future improvements

-   Add rules to make it a real game.
-   Allow the player to choose the appearance of their character.
