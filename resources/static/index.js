document.addEventListener('DOMContentLoaded', () => {
    const sendMessageButton = document.getElementById("send-msg-btn");
    const messageInput = document.getElementById("input-msg");
    const messageContainer = document.getElementById("messages");
    const usernameInput = document.getElementById("input-username");
    const sendUsernameButton = document.getElementById("send-username-btn");
    const usernameSection = document.getElementById("username-section");
    const chatSection = document.getElementById("chat-section");
    const usersSection = document.getElementById("users");

    const addMessage = messageBody => {
        const newMessageContainer = document.createElement("div");
        newMessageContainer.classList.add("message-container");

        const topContainer = document.createElement("div");
        topContainer.classList.add("message-top");

        const senderContainer = document.createElement("div");
        senderContainer.classList.add("sender");
        senderContainer.textContent = messageBody.senderUsername;

        const dateContainer = document.createElement("div");
        dateContainer.classList.add("date");

        const dateObj = new Date(messageBody.timeSent);
        const options = {
          hour: 'numeric',
          minute: '2-digit',
          hour12: true,
          month: 'short',
          day: 'numeric'
        };
        const formattedDate = dateObj.toLocaleString('en-US', options);
        dateContainer.textContent = formattedDate;

        topContainer.appendChild(senderContainer);
        topContainer.appendChild(dateContainer);

        const messageTextContainer = document.createElement("div");
        messageTextContainer.classList.add("message");
        messageTextContainer.textContent = messageBody.message;

        newMessageContainer.appendChild(topContainer);
        newMessageContainer.appendChild(messageTextContainer);

        messageContainer.appendChild(newMessageContainer);
        newMessageContainer.scrollIntoView({
            "behavior": "smooth"
        });
    };

    const addUser = user => {
        const newUserContainer = document.createElement("div");
        newUserContainer.classList.add("user");
        newUserContainer.setAttribute("id", `user${user.userId}`)
        newUserContainer.textContent = user.username;
        usersSection.appendChild(newUserContainer);
    };

    const removeUser = user => {
        const userElementToRemove = document.getElementById(`user${user.userId}`);
        if (userElementToRemove) {
            userElementToRemove.parentNode.removeChild(userElementToRemove);
        }
    };

    const setupSocket = () => {
        let socket = new SockJS('/ws');
        let stompClient = Stomp.over(socket);

        const onMessageReceived = payload => {
            const payloadBody = JSON.parse(payload.body);
            const messageBody = payloadBody.body;
            console.log("messageBody: ", messageBody);
            if (messageBody.type === "NEW_MESSAGE") {
                addMessage(payloadBody.body);
            } else if (messageBody.type === "JOIN") {
                if (messageBody.userId !== storedUser.id) {
                    addUser(messageBody)
                }
            } else if (messageBody.type == "DISCONNECT") {
               if (messageBody.userId !== storedUser.id) {
                    removeUser(messageBody);
               }
            }
        };

        const onConnected = async () => {
            await stompClient.subscribe('/topic/public', onMessageReceived);

            stompClient.send("/app/chat.userJoin", {}, JSON.stringify({
                userId: storedUser.id,
                username: storedUser.username
            }));
        };

        const onError = error => {
            console.error('Could not connect to WebSocket: ', error);
        };

        stompClient.connect({}, onConnected, onError);

        sendMessageButton.addEventListener('click', e => {
            if (!messageInput || messageInput.value === "") {
                window.alert("Message cannot be empty!");
                return;
            }
            const chatMessage = {
                message: messageInput.value,
                senderId: storedUser.id
            };
            stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));
            messageInput.value = "";
        });
    };

    const getPreviousMessages = async () => {
        try {
            const response = await fetch('/previous/messages', {
                method: 'GET'
            });
            if (!response.ok)
                throw new Error("Failed to retrieve previous messages!");
            const prevMessages = await response.json();
            prevMessages.forEach(addMessage);
        } catch (error) {
            window.alert(error.message);
        }
    };

    const getConnectedUsers = async () => {
        try {
            const response = await fetch('/connected/users', {
                method: 'GET'
            });
            if (!response.ok)
                throw new Error("Failed to retrieve connected users!");
            let connectedUsers = await response.json();
            connectedUsers = connectedUsers.filter(connectedUser => connectedUser.userId !== storedUser.id);
            connectedUsers.forEach(addUser);
        } catch (error) {
            window.alert(error.message);
        }
    };

    let storedUserStr = localStorage.getItem("user");
    let storedUser = null;
    if (storedUserStr !== null) {
        storedUser = JSON.parse(storedUserStr);
        usernameSection.style.display = "none";
        chatSection.style.display = "block";
        usersSection.style.display = "block";
        setupSocket();
        getPreviousMessages();
        getConnectedUsers();
    }

    const sendUsername = async () => {
        try {
            if (!usernameInput || usernameInput.value === "" || usernameInput.value.trim() === "") {
                window.alert("Username must not be empty!");
                return;
            }
            const response = await fetch("/user/register", {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    username: usernameInput.value
                })
            })
            if (!response.ok) {
                throw new Error("Response not ok!");
            }
            const responseBody = await response.json();
            //localStorage.setItem("user", JSON.stringify(responseBody));
            storedUser = responseBody;
            usernameSection.style.display = "none";
            chatSection.style.display = "block";
            usersSection.style.display = "block";
            setupSocket();
            getPreviousMessages();
            getConnectedUsers();
        } catch (error) {
            window.alert(error.message);
        }
    };

    sendUsernameButton.addEventListener("click", _ => sendUsername());

    usernameInput.addEventListener("keydown", event => {
        if (event.key === 'Enter') {
            event.preventDefault();
            sendUsername();
        }
    });
})

