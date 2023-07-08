document.addEventListener('DOMContentLoaded', () => {
    const sendMessageButton = document.getElementById("send-msg-btn");
    const messageInput = document.getElementById("input-msg");
    const messageContainer = document.getElementById("messages");
    const usernameInput = document.getElementById("input-username");
    const sendUsernameButton = document.getElementById("send-username-btn");
    const usernameSection = document.getElementById("username-section");
    const chatSection = document.getElementById("chat-section");
    const usersSection = document.getElementById("users");
    const publicChatButton = document.getElementById("public-chat-btn");
    const chatWithDiv = document.getElementById("chat-with");

    let publicChatEnabled = true;
    let receivingUser = null;

    const getPreviousMessages = async () => {
        try {
            let url = `/previous/messages?senderId=${storedUser.id}`;
            if (receivingUser) {
                url += `&receiverId=${receivingUser.userId}`;
            }
            const response = await fetch(url, {
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

    const addMessage = messageBody => {
        const publicChatCondition = (publicChatEnabled && !messageBody.receiverUserId);
        const privateChatCondition = (
            !publicChatEnabled
            && messageBody.receiverUserId
            && receivingUser
            && (
                (messageBody.senderUserId === storedUser.id && messageBody.receiverUserId === receivingUser.userId)
                || (messageBody.senderUserId === receivingUser.userId && messageBody.receiverUserId === storedUser.id)
            )
        );
        if (!publicChatCondition && !privateChatCondition)
            return;
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
        newUserContainer.addEventListener('click', e => {
            messageContainer.innerHTML = '';
            chatWithDiv.textContent = user.username;
            receivingUser = user;
            publicChatEnabled = false;
            getPreviousMessages();
        })
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

        const messageFunc = () => {
            if (!messageInput || messageInput.value === "") {
                window.alert("Message cannot be empty!");
                return;
            }
            const chatMessage = {
                message: messageInput.value,
                senderId: storedUser.id,
                receiverId: receivingUser ? receivingUser.userId : null
            };
            stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));
            messageInput.value = "";
        };

        sendMessageButton.addEventListener('click', _ => {
            messageFunc();
        });

        messageInput.addEventListener("keydown", event => {
            if (event.key === 'Enter') {
                event.preventDefault();
                messageFunc();
            }
        });
    };

    publicChatButton.addEventListener("click", _ => {
        if (publicChatEnabled) {
            receivingUser = null;
            return;
        }
        messageContainer.innerHTML = "";
        chatWithDiv.textContent = "Public chat";
        receivingUser = null;
        publicChatEnabled = true;
        getPreviousMessages();
    });

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

