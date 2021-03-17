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
    let expires = "";
    //let expires = (new Date(Date.now() + 525600 * 60 * 1000 * 5)).toUTCString();
    if (filled && pass && age) {
        let data = {"username": username,
                    "first_name": firstName,
                    "last_name": lastName,
                    "email": email,
                    "password_hash": hashedPassword
                };
        fetch("https://157.230.233.218:80/api/account/signup", {
            method: "POST",
	    	body: JSON.stringify(data),
	    	headers: {
	    		"Content-type": "application/json; charset=UTF-8"
	    	},
	    	mode: "cors",
	    	headers: {
	    		"Origin": "https://157.230.233.218"
	    	}
        })
        .then(res => {
            let code = res.status;
            //console.log(code);
            if (code === 403) {
                document.getElementById("usernameTaken").style.display = "block";
            } else {
                document.getElementById("usernameTaken").style.display = "none";
                return res.json()
            }
            console.log("Request complete!");
        })
        .then(json => {
            console.log(json);
		    token = json.token;
		    document.cookie = `token=${token}; expires=${expires}`;
		    document.cookie = `username=${username}; expires=${expires}`;
		    window.location.href = "/";
        })
        .catch(err => console.log(err));     
    }
}

function stringToHash(string) { 
    return string; 
}

onkeydown = function(e) {
	let key = e.key;
	if (key === "Enter") {
        if (document.getElementById("username") == document.activeElement) {
			document.getElementById("password").focus();
		} else if (document.getElementById("password") == document.activeElement) {
			document.getElementById("repassword").focus(); 
        } else if (document.getElementById("repassword") == document.activeElement) {
			document.getElementById("firstName").focus();
        } else if (document.getElementById("firstName") == document.activeElement) {
			document.getElementById("lastName").focus();
        } else if (document.getElementById("lastName") == document.activeElement) {
			document.getElementById("email").focus();
        } else {
		    checkForm();
        }
	}
}