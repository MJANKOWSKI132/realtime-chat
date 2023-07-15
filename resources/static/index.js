document.addEventListener('DOMContentLoaded', () => {
    const sendMessageButton = document.getElementById("send-msg-btn");
    const messageInput = document.getElementById("input-msg");
    const messageContainer = document.getElementById("messages");
    const usernameInput = document.getElementById("input-username");
    const passwordInput = document.getElementById("input-password");
    const sendUsernameButton = document.getElementById("send-username-btn");
    const usernameSection = document.getElementById("username-section");
    const chatSection = document.getElementById("chat-section");
    const usersSection = document.getElementById("users");
    const publicChatButton = document.getElementById("public-chat-btn");
    const chatWithDiv = document.getElementById("chat-with");

    const loginRegisterScreen = document.getElementById("login-register-screen");
    const loginButton = document.getElementById("login-btn");
    const registerButton = document.getElementById("register-btn");
    const backFromUsernamePasswordSectionButton = document.getElementById("back-from-username-section-btn");
    const logoutButton = document.getElementById("logout-btn");


    let loginProcessStarted = false;
    let registerProcessStarted = false;

    let publicChatEnabled = true;
    let receivingUser = null;

    backFromUsernamePasswordSectionButton.addEventListener("click", _ => {
        loginProcessStarted = false;
        registerProcessStarted = false;
        usernameSection.style.display = "none";
        loginRegisterScreen.style.display = "flex";
        usernameInput.value = "";
        passwordInput.value = "";
    });

    loginButton.addEventListener("click", _ => {
        loginProcessStarted = true;
        registerProcessStarted = false;
        usernameSection.style.display = "flex";
        loginRegisterScreen.style.display = "none";
        sendUsernameButton.textContent = "Login";
        usernameInput.focus();
    });
    registerButton.addEventListener("click", _ => {
        registerProcessStarted = true;
        loginProcessStarted = false;
        usernameSection.style.display = "flex";
        loginRegisterScreen.style.display = "none";
        sendUsernameButton.textContent = "Register";
        usernameInput.focus();
    });

    const getPreviousMessages = async () => {
        try {
            let url = `/previous/messages?senderId=${storedUser.id}`;
            if (receivingUser) {
                url += `&receiverId=${receivingUser.userId}`;
            }
            const response = await fetch(url, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${storedUser.token}`
                },
            });
            if (!response.ok)
                throw new Error("Failed to retrieve previous messages!");
            const prevMessages = await response.json();
            messageContainer.innerHTML = '';
            for (let prevMessage of prevMessages) {
                await addMessage(prevMessage);
            }
        } catch (error) {
            window.alert(error.message);
        }
    };

    logoutButton.addEventListener("click", async _ => {
        const confirm = window.confirm("Are you sure you want to logout?");
        if (!confirm)
            return;
        try {
            const response = await fetch('/user/logout', {
                method: 'PUT',
                headers: {
                    'Authorization': `Bearer ${storedUser.token}`
                }
            })
            const responseBody = await response.json();
            if (!response.ok)
                throw new Error(responseBody.message || "Failed to logout!");
            localStorage.removeItem("user");
            chatSection.style.display = "none";
            usersSection.style.display = "none";
            loginRegisterScreen.style.display = "flex";
        } catch (error) {
            window.alert(error.message);
        }
    })

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
        newUserContainer.classList.add("user-container");
        newUserContainer.setAttribute("id", `user-container${user.userId}`)

        const usernameContainer = document.createElement("div");
        usernameContainer.classList.add("user");
        usernameContainer.setAttribute("id", `user${user.userId}`)
        usernameContainer.textContent = user.username;

        const newMessageCounter = document.createElement("div");
        newMessageCounter.classList.add("new-message-counter");
        newMessageCounter.setAttribute("id", `new-message-counter${user.userId}`)
        newMessageCounter.style.visibility = "hidden";
        newMessageCounter.textContent = "0";

        newUserContainer.addEventListener('click', async e => {
            messageContainer.innerHTML = '';
            chatWithDiv.textContent = user.username;
            receivingUser = user;
            publicChatEnabled = false;

            await getPreviousMessages();

            const newMessageCounter = document.getElementById(`new-message-counter${user.userId}`);
            newMessageCounter.textContent = "0";
            newMessageCounter.style.visibility = "hidden";
        })

        newUserContainer.appendChild(usernameContainer);
        newUserContainer.appendChild(newMessageCounter);

        usersSection.appendChild(newUserContainer);
    };

    const removeUser = user => {
        const userElementToRemove = document.getElementById(`user-container${user.userId}`);
        if (userElementToRemove) {
            userElementToRemove.parentNode.removeChild(userElementToRemove);
        }
    };

    const setupSocket = () => {
        let socket = new SockJS('/ws');
        let stompClient = Stomp.over(socket);

        const onMessageReceived = payload => {
            const payloadBody = JSON.parse(payload.body);
            if (payloadBody.statusCodeValue < 200 || payloadBody.statusCodeValue > 299) {
                window.alert("Cannot receive messages at this moment!");
                chatSection.style.display = "none";
                usersSection.style.display = "none";
                loginRegisterScreen.style.display = "flex";
                return;
            }
            const messageBody = payloadBody.body;
            if (messageBody.type === "NEW_MESSAGE") {
                if (messageBody.senderUserId !== storedUser.id && messageBody.receiverUserId === storedUser.id && (publicChatEnabled || receivingUser.userId !== messageBody.senderUserId)) {
                    const newMessageCounter = document.getElementById(`new-message-counter${messageBody.senderUserId}`);
                    const val = parseInt(newMessageCounter.textContent);
                    newMessageCounter.style.visibility = "visible";
                    newMessageCounter.textContent = `${val + 1}`;

                    const matchingUserContainer = document.getElementById(`user-container${messageBody.senderUserId}`);
                    const parentNode = matchingUserContainer.parentNode;
                    parentNode.removeChild(matchingUserContainer);
                    parentNode.insertBefore(matchingUserContainer, parentNode.firstChild);
                }
                if (receivingUser && receivingUser.userId === messageBody.receiverUserId) {
                    const matchingUserContainer = document.getElementById(`user-container${messageBody.receiverUserId}`);
                    const parentNode = matchingUserContainer.parentNode;
                    parentNode.removeChild(matchingUserContainer);
                    parentNode.insertBefore(matchingUserContainer, parentNode.firstChild);
                }
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

        const onConnected = () => {
            stompClient.subscribe('/topic/public', onMessageReceived);

            stompClient.send("/app/chat.userJoin", {'Authorization': `Bearer ${storedUser.token}`}, JSON.stringify({
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
                receiverId: receivingUser ? receivingUser.userId : null
            };
            stompClient.send("/app/chat.sendMessage", {'Authorization': `Bearer ${storedUser.token}`}, JSON.stringify(chatMessage));
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
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${storedUser.token}`
                },
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
        if (!storedUser.token)
            return;
        fetch('/ping', {
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${storedUser.token}`
            }
        })
        .then(res => {
            if (!res.ok) {
                localStorage.removeItem("user");
                return;
            }
            usernameSection.style.display = "none";
            chatSection.style.display = "block";
            usersSection.style.display = "block";
            loginRegisterScreen.style.display = "none";
            setupSocket();
            getPreviousMessages();
            getConnectedUsers();
        })
    }

    const sendUsername = async () => {
        try {
            if (!usernameInput || usernameInput.value === "" || usernameInput.value.trim() === "") {
                window.alert("Username must not be empty!");
                return;
            }
            if (!passwordInput || passwordInput.value === "" || passwordInput.value.trim() === "") {
                window.alert("Password input must not be empty!");
                return;
            }
            if (loginProcessStarted) {
                const response = await fetch("/user/signin", {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        username: usernameInput.value,
                        password: passwordInput.value
                    })
                })
                const responseBody = await response.json();
                if (!response.ok) {
                    throw new Error(responseBody.message || "Failed to login!");
                }
                localStorage.setItem("user", JSON.stringify(responseBody));
                storedUser = responseBody;
                usernameSection.style.display = "none";
                chatSection.style.display = "block";
                usersSection.style.display = "block";
                setupSocket();
                getPreviousMessages();
                getConnectedUsers();
                usernameInput.value = "";
                passwordInput.value = "";
                messageInput.focus();
            } else if (registerProcessStarted) {
                const response = await fetch("/user/register", {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                        username: usernameInput.value,
                        password: passwordInput.value
                    })
                })
                const responseBody = await response.json();
                if (!response.ok) {
                    throw new Error(responseBody.message || "Failed to register!");
                }
                usernameSection.style.display = "none";
                loginProcessStarted = true;
                registerProcessStarted = false;
                loginRegisterScreen.style.display = "flex"
                usernameInput.value = "";
                passwordInput.value = "";
                window.alert(`Successfully registered the user with username ${responseBody.username}`);
            }
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

