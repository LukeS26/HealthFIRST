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
			displayPost(json, url);
		})
		.catch(function (error) {
			console.log(error);

			return null;
		});
}

function displayPost(post, id) {

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

	let userImg = "";
	if (post.author === getCookie("username")) {
		let cookie;
		if (getCookie("imgUrl") !== null) {
			cookie = getCookie("imgUrl");
		} else {
			cookie = "defaultPic.png";
		}
		userImg = `<div class="profileImage" style="width: 30px; height: 30px; overflow: hidden; display: inline-block; position: relative; top: 8px;"><img src="${cookie}" height="30px" width="30px"></div>`;
	}

	html += `<div tabindex="0" id="postOpen" onclick="loadPost('${id}')"><h1 class='postTitle'>${post.title}</h1>`;
	html += `<a class='postAuthor' href='/user.html?${post.author}' >${userImg}<span style="padding-left: 5px">${post.author}<span></a>`;
	html += `<h6 class='postDate'>${date}</h6>`
	html += `<p class='postBody'>${formatText(body)}</p> </div>`;

	container.innerHTML += html;


	document.getElementById("posts").appendChild(container);
	postCount++;
}

function loadPost(id) {
	window.location.assign("/post.html?id=" + id);
}


//getPosts("602878639903f175355bd339");

function loadPage(page) {
	fetch("http://157.230.233.218:8080/api/posts/feed?page=" + page)
	.then(res => res.json())
	.then(function(json) { 
		console.log(page);

		for(let i = 0; i < json["feed"].length; i++) {
			//console.log(json["feed"][i]);
			displayPost(json["feed"][i], json["feed"][i]["_id"]["$oid"])
		}

		if(document.getElementById("loadingPost")) {
			document.getElementById("loadingPost").remove();
		}
	} );
}

loadPage(0);

window.onscroll = function(ev) {
    if ((window.innerHeight + window.scrollY) >= document.body.offsetHeight) {
		page++;
		loadPage(page);
    }
};

function collectPostInfo() {
	makePost(document.getElementById("postTitle").value, document.getElementById("postBody").value);
	window.location.reload();
}

function makePost(title, body) {
	fetch("http://157.230.233.218:8080/api/posts", {
		method: "POST",
		body: JSON.stringify({"title":title, "body": body}),
		mode: "cors",
		headers: {
			"Content-type": "application/json; charset=UTF-8",
			"Authorization": getCookie("token"),
			"Origin": "http://157.230.233.218"
		}
	});
}

function togglePostPopup() {
	let blur = document.getElementById("popupBlur");
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
function formatText(text) {
	text = text.split(" ");
	text = text.join("&nbsp;")
	text = text.split("**");

	for(let i = 0; i < text.length; i++) {
		if(i % 2 != 0) {
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