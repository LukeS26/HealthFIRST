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
	};

	let expires = document.getElementById("keepLoggedIn").checked;
	if (expires) {
		expires = (new Date(Date.now() + 525600 * 60 * 1000 * 5)).toUTCString();
	} else {
		expires = "";
	}

	fetch("http://157.230.233.218:8080/api/account/login", {
		method: "POST",
		body: JSON.stringify(data),
		headers: {
			"Content-type": "application/json; charset=UTF-8"
		},
		mode: "cors",
		headers: {
			"Origin": "http://healthfirst4342.tk/"
		}
	})
		.then(res => {
			let code = res.status;
			if (code === 404) {
				document.getElementById("notFound").style.display = "block";
			} else if (code === 403) {
				document.getElementById("notFound").style.display = "block";
			} else {
				document.getElementById("notFound").style.display = "none";
				return res.json();
			}
		})
		.then(function (json) {
			//console.log(json);
			token = json.token;
			document.cookie = `token=${token}; expires=${expires}`;
			document.cookie = `username=${json.username}; expires=${expires}`;
			document.cookie = `cookieGoneDate=${expires}; expires=${expires}`;
			let url = "http://157.230.233.218:8080/api/account/" + json.username;
			fetch(url)
				.then(res => res.json())
				.then(json => {
					document.cookie = `imgUrl=${json.profile_picture_link}; expires=${expires}`;
					window.location.href = "/";
				})
				.catch(function (error) {
					console.log(error);
				});
		})
		.catch(err => console.log(err));
}

onkeydown = function (e) {
	let key = e.key;
	if (key === "Enter") {
		if (document.getElementById("username") == document.activeElement) {
			document.getElementById("password").focus();
		} else {
			checkForm();
		}
	}
}


function toggleVisibility() {
	let temp = document.getElementById("password");
	if(temp.type == "text") {
		temp.type = "password";
		document.getElementById("passVis").src = "/images/eye.svg";
	} else {
		temp.type = "text";
		document.getElementById("passVis").src = "/images/eyecross.svg";
	}
}