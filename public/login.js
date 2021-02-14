let username, password;
let filled = false;

function checkForm() {
    username = document.getElementById("username").value;
    password = document.getElementById("password").value;

    if (username === "" || password === "") {
        document.getElementById("notFilled").style.display = "block";
        filled = false;
    } else {
        document.getElementById("notFilled").style.display = "none";
        filled = true;
    }

    let hashedPassword = stringToHash(password);
    if (filled) {
        checkUser("http://157.230.233.218:8080/api/profile/" + username);
    }
}

function stringToHash(string) { 
    return string; 
}

function checkUser(url) {
	fetch(url)
		.then((resp) => resp.json)
		.then(function (data) {
			let userData = data.results;
            console.log(userData);
		})
		.catch(function (error) {
			console.log(error);
		});
}