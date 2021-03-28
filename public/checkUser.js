function getCookie(name) {
	let cookieArr = document.cookie.split(";");

	for (let i = 0; i < cookieArr.length; i++) {
		let cookiePair = cookieArr[i].split("=");

		if (name === cookiePair[0].trim()) {
			return cookiePair[1];
		}
	}

	return null;
}

if (getCookie("accepted") !== "true") {
	let data = {}
	let currToken = getCookie("token");

	fetch("http://healthfirst4342.tk:8080/api/token/verify", {
		method: "POST",
		body: JSON.stringify(data),
		mode: "cors",
		headers: {
			'Content-type': 'application/json; charset=UTF-8',
			'Authorization': currToken,
			'Origin': 'http://157.230.233.218:8080'
		}
	})
	.then(res => {
		let code = res.status;
		if (!res.ok) {
			window.location.href = "/login.html";
		} else {
			document.cookie = "accepted=true";
		}
	})
	.catch(err => {
		console.log(err);
		console.log("Request failed!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
	});
}

fetch("http://healthfirst4342.tk:8080/api/account/" + getCookie("username"))
.then(res => res.json())
.then(json => console.log(json))
.catch(err => console.error(err));