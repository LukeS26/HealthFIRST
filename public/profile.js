let profileOut = document.getElementById("profileOut");
let blurColor;
let blurOpen = false;
let imgUrl = "";
let light = true;

let url = "http://157.230.233.218:8080/api/account";
fetch(url, {
	method: "GET",
	mode: "cors",
	headers: {
		"Content-type": "application/json; charset=UTF-8",
		"Authorization": getCookie("token"),
		"Origin": "http://healthfirst4342.tk/"
	}
})
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
	let profileContent = "";
	if (info.profile_picture_link !== null) {
		profileContent += `<div class="profileImage" style="width: 100px; height: 100px; overflow: hidden;"><img src="${info.profile_picture_link}" height="100px" width="100px"></div>`;
	} else {
		profileContent += `<img src="defaultPic.png" height="100px" width="100px">`;
	}

	profileContent += "<div>Username: " + (info.username) + "</div>";
	profileContent += "<div>Name: " + info.first_name + " " + info.last_name + "</div>";
	profileContent += "<div>Email: " + info.email + "</div>";
	//profileContent += info.profile_picture_link + "<br>";
	//profileContent += info.permission_id + "<br>";
	//profileContent += info.badge_ids + "<br>";

	profileOut.innerHTML = profileContent;
}

function changeProfilePic() {
	let url = document.getElementById("imgUrlInput").value;

	if (url === "" && imgUrl !== "") {
		url = imgUrl;
	}

	if (url !== "") {
		let data = {
			profile_picture_link: url
		};
		let currToken = getCookie("token");
	
		fetch("http://157.230.233.218:8080/api/account/", {
			method: "PATCH",
			body: JSON.stringify(data),
			mode: "cors",
			headers: {
				'Content-type': 'application/json; charset=UTF-8',
				'Authorization': currToken,
				'Origin': 'http://157.230.233.218:8080'
			}
		})
		.then(res => {
			if (res.ok) {
				document.cookie = `imgUrl=${url}; expires=${getCookie("cookieGoneDate")}`; 
				window.location.reload();
			}
		})
		.catch(err => console.log(err));
	}
}

function drop(e) {
	e.preventDefault();
	let data = e.dataTransfer;
	let files = data.files;
	
	if (files.length) {
		console.log("Local Files Not Supported Yet");
	} else {
		let img = data.getData("text/html");
		let match = img && /\bsrc="?([^"\s]+)"?\s*/.exec(img);
    	imgUrl = match && match[1];
		document.getElementById("dropBox").innerHTML = `<img src=${imgUrl} width="200px" height="100px" style="padding: 20px">`;
	}
}

function allowDrop(e) {
	e.preventDefault();
}

function togglePhotoPopup() {
	let blur = document.getElementById("imagePopupContainer");
	if (!blurOpen) {
		blur.style.display = "block";
		blurColor = "rgba(211, 211, 211, 0.6)";
		window.setTimeout(setBlurColor, 1);
		blurOpen = true;
		//document.getElementsByTagName("body")[0].style.filter = "blur(4px)";
		//blur.style.filter = "none";
	} else {
		blur.style.backgroundColor = "rgba(211, 211, 211, 0)";
		blur.style.display = "none";
		blurOpen = false;
		//document.getElementsByTagName("body")[0].style.filter = "none";
	}
}

function setBlurColor() {
	let blur = document.getElementById("popupBlur");
	blur.style.backgroundColor = blurColor;
}

if( navigator.userAgent.match(/Android/i)
 || navigator.userAgent.match(/webOS/i)
 || navigator.userAgent.match(/iPhone/i)
 || navigator.userAgent.match(/iPad/i)
 || navigator.userAgent.match(/iPod/i)
 || navigator.userAgent.match(/BlackBerry/i)
 || navigator.userAgent.match(/Windows Phone/i)) {
	let dropBox = document.getElementById("dropBox");
	let picOr = document.getElementById("picOr");
	let imgUrlInput = document.getElementById("imgUrlInput");
	dropBox.remove();
	picOr.remove();
	imgUrlInput.style.marginTop = "10px";
	imgUrlInput.style.width = "90%";
}

// async function formatText(text) {
// 	let response = await fetch('https://api.github.com/markdown', {method:"POST", body: JSON.stringify({"text": text}) } );//.then(res => res.text()).then(function(json) {return (json)})
// 	let json = await response.text();

// 	return json;
// }

function toggleLightMode() {
	if (light) {
		darkMode();
		light = false;
		localStorage.setItem("light-mode", "dark");
	} else {
		lightMode();
		light = true;
		localStorage.setItem("light-mode", "light");
	}
}

function darkMode() {
	let body = document.getElementsByTagName("body")[0];
	let htmlBody = document.getElementsByTagName("body");
	let header = document.getElementsByTagName("header")[0];
	let profileHeader = document.getElementById("profileHeader");

	body.style.transition = "linear 0.5s";
	htmlBody[0].style.backgroundColor = "rgb(25, 25, 25)";
	htmlBody[0].style.color = "white";
	header.style.backgroundColor = "#002672";
	profileHeader.style.backgroundColor = "#002672";
}

function lightMode() {
	let body = document.getElementsByTagName("body")[0];
	let htmlBody = document.getElementsByTagName("body");
	let header = document.getElementsByTagName("header")[0];
	let profileHeader = document.getElementById("profileHeader");

	body.style.transition = "linear 0.5s";
	htmlBody[0].style.backgroundColor = "white";
	htmlBody[0].style.color = "black";
	header.style.backgroundColor = "#0044CC";
	profileHeader.style.backgroundColor = "#0044CC";
}

if (localStorage.getItem("light-mode") === "light") {
	document.getElementsByTagName("body")[0].style.backgroundColor = "white";
	document.getElementById("lightModeSlider").innerHTML = `<input type="checkbox" onchange="toggleLightMode()"><div class="slider"></div>`;
} else {
	document.getElementById("lightModeSlider").innerHTML = `<input type="checkbox" onchange="toggleLightMode()" checked><div class="slider"></div>`;
	light = false;
}