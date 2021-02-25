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

    let hashedPassword = stringToHash(password);
    if (filled && pass && age) {
        let data = {"username": username,
                    "first_name": firstName,
                    "last_name": lastName,
                    "email": email,
                    "password_hash": hashedPassword
                };
        fetch("http://157.230.233.218:8080/api/account/signup", {
            method: "POST",
	    	body: JSON.stringify(data),
	    	headers: {
	    		"Content-type": "application/json; charset=UTF-8"
	    	},
	    	mode: "no-cors",
	    	headers: {
	    		"Origin": "http://157.230.233.218"
	    	}
        }).then(res => {
            console.log("Request complete!");
        })        
    }
}

function stringToHash(string) { 
    return string; 
}

onkeydown = function(e) {
	let key = e.key;
	if (key === "Enter") {
		checkForm();
	}
}