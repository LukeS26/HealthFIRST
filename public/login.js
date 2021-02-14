let username, password, keepLoggedIn;
let filled = false;

function checkForm() {
    username = document.getElementById("username").value;
    password = document.getElementById("password").value;
    keepLoggedIn = document.getElementById("keepLoggedIn").checked;

    if (username === "" || password === "") {
        document.getElementById("notFilled").style.display = "block";
        filled = false;
    } else {
        document.getElementById("notFilled").style.display = "none";
        filled = true;
    }

    if (filled) {
        if (keepLoggedIn) {

        }
        checkUser();
    }
}

function stringToHash(string) { 
    return string; 
}

function checkUser() {
    let hashedPassword = stringToHash(password);
    let data = {
        "username": username,
        "password_hash": hashedPassword,
        "expire": keepLoggedIn
    }

    fetch("http://157.230.233.218:8080/api/account/login", {
        method: "POST",
        body: JSON.stringify(data)
    })
    .then(res => {
        console.log("Request complete!");
    })
    .then((resp) => resp.json)
    .then(function (data) {
        let userData = data.results;
        console.log(userData);
    })
    .catch(function (error) {
        console.log(error);
    });
}