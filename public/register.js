let username, password, repassword, firstName, lastName, email, check13;
let filled = false, pass = false, age = false;
document.getElementById("check13").value = "off";

function checkForm() {
    username = document.getElementById("username").value;
    password = document.getElementById("password").value;
    repassword = document.getElementById("repassword").value;
    firstName = document.getElementById("firstName").value;
    lastName = document.getElementById("lastName").value;
    email = document.getElementById("email").value;
    check13 = document.getElementById("check13").checked;

    if (username === "" ||
        password === "" ||
        repassword === "" ||
        firstName === "" ||
        lastName === "" ||
        email === "") {
            document.getElementById("notFilled").style.display = "block";
            filled = false;
    } else {
        document.getElementById("notFilled").style.display = "none";
        filled = true;
    }
    if (password !== repassword) {
        document.getElementById("notMatch").style.display = "block";
        pass = false;
    } else {
        document.getElementById("notMatch").style.display = "none";
        pass = true;
    }
    if (!check13) {
        document.getElementById("check13Box").style.display = "block";
        age = false;
    } else {
        document.getElementById("check13Box").style.display = "none";
        age = true;
    }

    if (filled && pass && age) {
        sendInfo();
    }
}

function sendInfo() {
    
}