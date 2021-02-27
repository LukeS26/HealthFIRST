let profileOut = document.getElementById("profileOut");

let url = "http://157.230.233.218:8080/api/account/" + getCookie("username");
fetch(url)
		.then(res => res.json())
		.then(json => {
			profileOut.innerHTML = json.first_name;
		})
		.catch(function (error) {
			console.log(error);

			return null;
		});

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