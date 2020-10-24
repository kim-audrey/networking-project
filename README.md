# BBCA

The Better Basic Chat App (BBCA) is a combination of both a working server and client for anyone who would want to launch their own simplistic chatting application.

## Usage

### Server Side

When the server is first run, it will tell the user that it has started, along with the IP address and port which will be used by the client to connect. As the server continues to run, it will print messages to the terminal stating the name of each client that joins(?), along with any errors the server finds.

### Client Side

Upon starting the client-side application, the client will be asked to input the server's IP address and port number. The necessary credentials are shown on the server's terminal. If the server is live and the correct credentials are entered, you will be connected to the server, otherwise an exception is thrown. Upon connecting to a server, you will be prompted to enter a username.

>##### Usernames
<span style="font-size:90%"> Usernames must follow the following guidelines. If they enter an invalid username, the user will be prompted to enter another username till a valid one is entered. Usernames must follow the following naming conventions
>1. <span style="font-size:90%"> All characters must be alphanumeric.
2.<span style="font-size:90%"> The username must be nonempty
3.<span style="font-size:90%"> There must be no whitespace in the name

Once the user has entered a valid username they can now access the main part of the chatting app. 

#### Chatting
Every user that successfully joins the server will be inside the main and only chat room in this application. In this chat room the user can receive and send multiple messages

##### Received Messages
There are four distinct types of messages that a user can receive once they are properly connected.
1. ```[name] has joined.```<br>This message is sent each time a new client connects.
2. ```[name]: [msg].```<br>The most basic message showing the message and who sent it. These messages are seen by the whole server
3. ```[name] (private): [msg].```<br>Another message that can be sent to you. This message means only you can see it. Again it also shows the message and sender.
4. ```[name] has left.```<br>This message is sent each time a client disconnects from the server.
5. ```Current Chatters:```<br>This message displays a list of all current chatroom participants.


##### Commands
There are four commands that are possible to send in this server. All commands will be sent to the server upon pressing enter.
1. ```[msg]```<br> Simply typing your message out will automatically sent it out for everyone to see.
2. ```@username [msg]```<br>This command will send a message privately to the person specified. User may input any number of @tags at a time for a given private message. 
3. ```/quit```<br>This command will disconnect the user from the server.
4. ```/block @username```<br>This command will permanently block the user inputted. Upon blocking a user, the selected user will not be able to send private messages to the client and the client will not see the selected user's messages in the server.
&nbsp;&nbsp;> On the UI, instead of typing this command, users can open the participants panel and press the block button
5. ```/whoishere```<br>This command will display a list of all current chatroom participants. 
&nbsp;&nbsp;> On the UI, this will create a new window with a list of the participants


