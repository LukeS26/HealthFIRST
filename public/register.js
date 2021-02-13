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
    let data = {"username": username,
                "first_name": firstName,
                "last_name": lastName,
                "email": email,
                "password_hash": stringToHash(password)};
    fetch("http://157.230.233.218/api/account/signup:8080", {
        method: "POST",
        body: JSON.stringify(data)
    }).then(res => {
        console.log("Request complete!");
    })        
    }
}

function stringToHash(string) { 
                  
    var hash = 0; 
      
    if (string.length == 0) return hash; 
      
    for (i = 0; i < string.length; i++) { 
        char = string.charCodeAt(i); 
        hash = ((hash << 5) - hash) + char; 
        hash = hash & hash; 
    } 
      
    return hash; 
} 