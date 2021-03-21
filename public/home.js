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

	html += `<div tabindex="0" id="postOpen" onclick="loadPost('${id}')"><h1 class='postTitle'>${post.title}</h1>`;
	html += `<a class='postAuthor' href='/user.html?${post.author}' >${post.author}</a>`;
	html += `<h6 class='postDate'>${date}</h6>`
	html += `<p class='postBody'>${body}</p> </div>`;

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
	} );
}

loadPage(0);

window.onscroll = function(ev) {
    if ((window.innerHeight + window.scrollY) >= document.body.offsetHeight) {
		page++;
		loadPage(page);
    }
};

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