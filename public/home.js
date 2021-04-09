let postCount = 0;
let open = [];
let page = 0;
let blurOpen = false;

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

function getPosts(url) {
	let fetchUrl = "http://157.230.233.218:8080/api/posts/" + url;
	fetch(fetchUrl)
		.then(res => res.json())
		.then(function (json) {
			displayPost(json, url, false);
		})
		.catch(function (error) {
			console.log(error);

			return null;
		});
}

function displayPost(post, id, top) {
	let html = "";
	let container = document.createElement("div");
	container.className = "postContainer";
	container.id = "post" + postCount;

	//caps post to 500 chars in home menu
	let body = post.body.slice(0, 500);
	if (body.length === 500) {
		body += "...";
	}
	let dateRaw = new Date(post.date.$date);
	let date = dateRaw.toLocaleString();

	// let userImg = "";
	// if (post.author === getCookie("username")) {
	// 	let cookie;
	// 	if (getCookie("imgUrl") !== null) {
	// 		cookie = getCookie("imgUrl");
	// 	} else {
	// 		cookie = "defaultPic.png";
	// 	}
	// 	userImg = `<div class="profileImage" style="width: 30px; height: 30px; overflow: hidden; display: inline-block; position: relative; top: 8px;"><img src="${cookie}" height="30px" width="30px"></div>`;
	// } else {
	// 	fetch(`http://157.230.233.218:8080/api/account/${post.author}`)
	// 		.then(res => res.json())
	// 		.then(function (json) {
	// 			document.getElementById("postImg" + postCount).innerHTML = `<div class="profileImage" style="width: 30px; height: 30px; overflow: hidden; display: inline-block; position: relative; top: 8px;"><img src="${json.profile_picture_link}" height="30px" width="30px"></div>`;
	// 		});
	// }
	if (post.author === getCookie("username") || getCookie("level") > 1) {
		html += `<span onClick='deletePost("${id}", ${postCount})' class="postOptions">`;
		html += `<div class="postToolTip">Delete</div>`;
		if (localStorage.getItem("light-mode") === "light") {
			html += `<img src="trash-can.png" width="20px" height="20px">`;
		} else {
			html += `<img src="darkModeTrashCan.png" width="20px" height="20px">`;
		}
		html += `</span>`;
	}

	if (post.author === getCookie("username")) {
		html += `<span class="postOptions" onclick="editPost('post${postCount}')">`;
		html += `<div class="postToolTip">Edit</div>`;
		if (localStorage.getItem("light-mode") === "light") {
			html += `<img src="pencil.png" width="20px" height="20px">`;
		} else {
			html += `<img src="darkModePencil.png" width="20px" height="20px">`;
		}
		html += `</span>`;
	}

	html += `<div tabindex="0" class="postOpen" onclick="loadPost('${id}')"><h1 class='postTitle'>${post.title}</h1>`;
	//<span id="postImg${postCount}">${userImg}</span>
	html += `<a class='postAuthor' href='/user.html?${post.author}' ><span style="padding-left: 5px">${post.author}<span></a>`;
	html += `<h6 class='postDate'>${date}</h6>`
	html += `<p class='postBody'>${formatText(body)}</p> </div>`;
	container.innerHTML += html;
	if (top) {
		document.getElementById("posts").prepend(container);
	} else {
		document.getElementById("posts").appendChild(container);
	}
	postCount++;
}

function loadPost(id) {
	window.location.assign("/post.html?id=" + id);
}


//getPosts("602878639903f175355bd339");

function loadPage(page) {
	fetch("http://157.230.233.218:8080/api/posts/feed?page=" + page)
		.then(res => res.json())
		.then(function (json) {

			for (let i = 0; i < json["feed"].length; i++) {
				//console.log(json["feed"][i]);
				displayPost(json["feed"][i], json["feed"][i]["_id"]["$oid"], false)
			}

			if (document.getElementById("loadingPost")) {
				document.getElementById("loadingPost").remove();
			}
		});
}

loadPage(0);

window.onscroll = function (ev) {
	if ((window.innerHeight + window.scrollY) >= document.body.offsetHeight) {
		page++;
		loadPage(page);
	}
};

function collectPostInfo() {
	let canPost = true;
	let canPostBody = true;

	if (document.getElementById("postTitle").value.length > 40) {
		//too long of a title
		document.getElementById("titleTooLong").style.display = "block";
		canPost = false;
	} else {
		document.getElementById("titleTooLong").style.display = "none";
		canPost = true;
	}

	if (document.getElementById("postBody").textContent.length > 3000) {
		//too long of a title
		document.getElementById("bodyTooLong").style.display = "block";
		canPostBody = false;
	} else {
		document.getElementById("bodyTooLong").style.display = "none";
		canPostBody = true;
	}

	if (canPost && canPostBody) {
		makePost(document.getElementById("postTitle").value, document.getElementById("postBody").innerHTML);
	}
}

function deleteAccount(username) {
	fetch("http://157.230.233.218:8080/api/account/" + username, {
		method: "DELETE",
		mode: "cors",
		headers: {
			"Content-type": "application/json; charset=UTF-8",
			"Authorization": getCookie("token"),
			"Origin": "http://healthfirst4342.tk/"
		}
	});
}

function makePost(title, body) {
	fetch("http://157.230.233.218:8080/api/posts", {
		method: "POST",
		body: JSON.stringify({ "title": title, "body": body }),
		mode: "cors",
		headers: {
			"Content-type": "application/json; charset=UTF-8",
			"Authorization": getCookie("token"),
			"Origin": "http://healthfirst4342.tk/"
		}
	})
		.then(res => res.text())
		.then(text => {
			let postData = {
				body: body,
				title: title,
				author: getCookie("username"),
				date: { $date: new Date().getTime() }
			}
			displayPost(postData, text, true);
			togglePostPopup();
			document.getElementById("postTitle").value = "";
			document.getElementById("postBody").value = "";
		})
		.catch(err => {
			console.error(err);
		})
}

function editPost(id) {
	let post = document.getElementById(id);
	let postTitleIn = document.getElementById("postTitle");
	let postBodyIn = document.getElementById("postBody");
	
	postTitleIn.value = post.getElementsByClassName("postOpen")[0].getElementsByClassName("postTitle")[0].textContent;
	postBodyIn.innerHTML = post.getElementsByClassName("postOpen")[0].getElementsByClassName("postBody")[0].innerHTML;

	togglePostPopup();
}

function togglePostPopup() {
	let blur = document.getElementById("postPopupContainer");
	let popup = document.getElementById("post-popup");
	if (!blurOpen) {
		blur.style.display = "block";
		blurColor = "rgba(211, 211, 211, 0.6)";
		window.setTimeout(setBlurColor, 1);
		blurOpen = true;
		popup.style.display = "block";
		document.getElementById("postTitle").focus();
		//document.getElementsByTagName("body")[0].style.filter = "blur(4px)";
		//blur.style.filter = "none";
	} else {
		blur.style.backgroundColor = "rgba(211, 211, 211, 0)";
		blur.style.display = "none";
		blurOpen = false;
		popup.style.display = "none";
		//document.getElementsByTagName("body")[0].style.filter = "none";
	}
}

function setBlurColor() {
	let blur = document.getElementById("popupBlur");
	blur.style.backgroundColor = blurColor;

}

function formatText(text) {
	// let response = await fetch('https://api.github.com/markdown', {method:"POST", body: JSON.stringify({"text": text}) } );//.then(res => res.text()).then(function(json) {return (json)})
	// let json = await response.text();
	// return json;

	text = text.split("\n").join("<br>");
	text = text.split(" ");
	text = text.join("&nbsp;");
	text = text.split("**");
	text = text.split("&lt;")
	text = text.join("<");
	text = text.split("&gt;")
	text = text.join(">");

	for (let i = 0; i < text.length; i++) {
		if (i % 2 != 0) {
			text[i] = "<b>" + text[i] + "</b>"
		}
	}

	text = text.join("").split("*");

	for (let i = 0; i < text.length; i++) {
		if (i % 2 != 0) {
			text[i] = "<i>" + text[i] + "</i>"
		}
	}

	return text.join("");
}

/*
function foo() {

	// your function code here

	setTimeout(foo, 5000);
}

foo();
*/


function deletePost(id, postNum) {
	if (confirm("Are you sure? This action cannot be undone.")) {
		fetch(`http://157.230.233.218:8080/api/posts/${id}`, {
			method: "DELETE",
			mode: "cors",
			headers: {
				"Content-type": "application/json; charset=UTF-8",
				"Authorization": getCookie("token"),
				"Origin": "http://healthfirst4342.tk/"
			}
		});
		let temp;
		try {
			temp = document.getElementById(`post${postNum}`).children[2].children;
		} catch {
			temp = document.getElementById(`post${postNum}`).children[1].children;
		}
		temp[0].innerHTML = "[Removed]";
		temp[1].innerHTML = "[Removed]";
		temp[1].href = "/user.html?[Removed]";
		temp[3].innerHTML = "[Removed]";
	}
}

/*
function darkMode() {
	let htmlBody = document.getElementsByTagName("body");
	let posts = document.getElementsByClassName("posts");
	let postContainers = document.getElementsByClassName("postContainer");
	let postAuthors = document.getElementsByClassName("postAuthor");
	let header = document.getElementsByTagName("header")[0];
	let profileHeader = document.getElementById("profileHeader");
	let navigation = document.getElementsByClassName("navigation")[0];

	htmlBody[0].style.backgroundColor = "rgb(25, 25, 25)";
	header.style.backgroundColor = "#002672";
	profileHeader.style.backgroundColor = "#002672";
	navigation.style.backgroundColor = "#002672";
	for (let i = 0; i < posts.length; i++) {
		posts[i].style.color = "white";
	}
	for (let i = 0; i < postContainers.length; i++) {
		postContainers[i].style.backgroundColor = "rgb(50, 50, 50)";
	}
	for (let i = 0; i < postAuthors.length; i++) {
		postAuthors[i].style.color = "white";
	}
}
*/