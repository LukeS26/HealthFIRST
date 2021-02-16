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
		"password": hashedPassword,
		"expire": !keepLoggedIn
	}

	fetch("http://157.230.233.218:8080/api/account/login", {
		method: "POST",
		body: JSON.stringify(data),
		headers: {
			"Content-type": "application/json; charset=UTF-8"
		},
		mode: "cors",
		headers: {
			'Origin': '157.230.233.218:8080/api/account/login'
		}
	}).then(function(response) {
		return response.json();
	}).then(function(data) {
		console.log(data);
	})
}