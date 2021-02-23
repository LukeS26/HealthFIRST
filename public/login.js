let username, password, keepLoggedIn;
let filled = false;
let token;

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
		"password": hashedPassword
	}

	fetch("http://157.230.233.218:8080/api/account/login", {
		method: "POST",
		body: (data),
		headers: {
			"Content-type": "application/json; charset=UTF-8"
		},
		mode: "no-cors"
	}).then(res => {
		let code = res.status;
		if (code === 404) {
			document.getElementById("usernameNotFound").style.display = "block";
			document.getElementById("passwordIncorrect").style.display = "none";
		} else if (code === 403) {
			document.getElementById("passwordIncorrect").style.display = "block";
			document.getElementById("usernameNotFound").style.display = "none";
		} else {
			document.getElementById("usernameNotFound").style.display = "none";
			document.getElementById("passwordIncorrect").style.display = "none";
			return res.json();
		}
	})
	.then(json => {
		token = json.token;
		document.cookie = `token=${token}; expires=${json.expire}`;
		document.cookie = `username=${username}; expires=${json.expire}`;
		window.location.href = "/";
	})
	.catch(err => {
		console.log("Request Failed!!!!!!!!!!!!!!!!!!!!!!!!")
		console.log(data);
	});
}

onkeydown = function(e) {
	let key = e.key;
	if (key === "Enter") {
		checkForm();
	}
}