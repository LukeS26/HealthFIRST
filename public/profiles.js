let profileOut = document.getElementById("profileOut");

let url = "http://157.230.233.218:8080/api/account/" + getCookie("username");
fetch(url)
		.then(res => res.json())
		.then(json => {
			generateHTML(json);
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

function generateHTML(info) {
	let profileContent = "hello";
	if (info.profile_picture_link !== null) {
		profileContent += `<img src="${info.profile_picture_link}"> height="100px width="100px"`;
	} else {
		profileContent += `<img src="defaultPic.png" height="100px" width="100px;`
	}

	profileOut.innerHTML = profileContent;
	/*
	profileContent += json.username + "<br>";
	profileContent += json.email + "<br>";
	profileContent += json.first_name + "<br>";
	profileContent += json.last_name + "<br>";
	profileContent += json.profile_picture_link + "<br>";
	profileContent += json.permission_id + "<br>";
	profileContent += json.badge_ids + "<br>";
	*/
}