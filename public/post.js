function getUrlVars() {
    var vars = {};
    var parts = window.location.href.replace(/[?&]+([^=&]+)=([^&]*)/gi, function(m,key,value) {
        vars[key] = value;
    });
    return vars;
}


let id = getUrlVars()["id"];
let comments = [];

function getPost(url) {
	let fetchUrl = "http://157.230.233.218:8080/api/posts/" + url;
	fetch(fetchUrl)
		.then(res => res.json())
		.then( function(json) {
			displayPost(json);
		})
		.catch (function (error) {
		console.log(error);

		return null;
	});
}

function getComments(url) {
	let fetchUrl = "http://157.230.233.218:8080/api/comments/" + url;
	fetch(fetchUrl)
	.then(res => res.json())
	.then(function (json) {
		console.log(json);
		comments = json["comments"];
		displayComments(comments);
	});
}

function getChildComments(comment) {
	let returnComments = [];

	for(let i = 0; i < comments.length; i++) {
		if(comments[i]["reply_to_id"] != null) {
			if(comments[i]["reply_to_id"]["$oid"] == comment["_id"]["$oid"]) {
				returnComments.push(comments[i]);
				let temp = getChildComments(comments[i]);
				if(temp.length > 0) {
					returnComments.push(temp);
				}
			}
		}
	}

	return returnComments;
}

function displayComments() {
	let commentsDisplay = [];

	for(let i = 0; i < comments.length;) {
		if(comments[i]["reply_to_id"] == null) {
			let temp = getChildComments(comments[i]);
			commentsDisplay.push(comments[i]);
			commentsDisplay.push(temp);
		}
		comments.shift()
		
	}

	console.log(commentsDisplay);
	
	formatReplies(commentsDisplay);

}

function displayPost(vals) {
	document.getElementById("title").innerHTML = vals.title;
	document.getElementById("author").innerHTML = vals.author;
	document.getElementById("author").href = "/user.html?" + vals.author;
	document.getElementById("date").innerHTML = new Date(vals.date.$date).toLocaleString();
	document.getElementById("body").innerHTML = vals.body;
}


let num = -2;
let display = "";

function formatReplies(replyArr) {
	num++;
	if (Array.isArray(replyArr)) {
		for (let i = 0; i < replyArr.length; i++) {
			if (!Array.isArray(replyArr[i])) {
				load(replyArr[i]["body"], num, replyArr[i]["author"], replyArr[i]["_id"]["$oid"]);
			}
			formatReplies(replyArr[i]);
			num--;
		}
	}
}

function load(reply, number, user, cid) {
	date = "DATE HERE"
	let comment = `<div name="${number}" id="${cid}" style="left: ${(30 * number) + 30}px; position: relative;" > <div style="display: flex;"> <a href="/user.html?${user}"> ${user} </a> <p style="width: 30%;position: relative;padding: 0 0 0 30px;margin: 0 0 0 0;"> ${date} </p> </div> <p> ${reply} </p> <div id="options"> <button onClick="openCommentField(this, '${cid}')" style="left: 25px;position: relative;"> Reply </button> </div> </div> `
	
	let shell = document.getElementById("comments");

	if(number > 0) {
		//find number - 1, and be that elements child
		let parentComment = document.getElementsByName(number - 1);
		parentComment[parentComment.length - 1].innerHTML += comment;
	} else {
		//be on outside edge
		shell.innerHTML += comment;
	}
}

function openCommentField(el, cid) {
	if(id = null) {
		//REPLYING TO POST
	} else {
		//REPLYING TO COMMENT
		let commentField = `<div> <input placeholder="Comment" id="inputField${cid}"> <button onClick="makeCommentFromSource('${cid}', this.parentElement.childNodes[1])"> Submit </button> <button onClick="this.parent.remove()"> Cancel </button> </div>`
		el.parentElement.parentElement.innerHTML += commentField;
	}
}

function makeComment(commentId, body) {
	let fetchUrl = "http://157.230.233.218:8080/api/comments/";
	fetch(fetchUrl, {
		method: "POST",
		body: JSON.stringify({ "reply_to_id":{"$oid": commentId}, "body": body, "post_id": id}),
		mode: "cors",
		headers: {
			"Content-type": "application/json; charset=UTF-8",
			"Authorization": getCookie("token"),
			"Origin": "http://157.230.233.218"
		}
	});
}

function makeCommentFromSource(commentId, source) {
	let fetchUrl = "http://157.230.233.218:8080/api/comments/";
	fetch(fetchUrl, {
		method: "POST",
		body: JSON.stringify({ "reply_to_id":{"$oid": commentId}, "body": source.value, "post_id": id}),
		mode: "cors",
		headers: {
			"Content-type": "application/json; charset=UTF-8",
			"Authorization": getCookie("token"),
			"Origin": "http://157.230.233.218"
		}
	});
}

function makeCommentOnPost(body) {
	let fetchUrl = "http://157.230.233.218:8080/api/comments/";
	fetch(fetchUrl, {
		method: "POST",
		body: JSON.stringify({ "body": body, "post_id": id}),
		mode: "cors",
		headers: {
			"Content-type": "application/json; charset=UTF-8",
			"Authorization": getCookie("token"),
			"Origin": "http://157.230.233.218"
		}
	});
}

getPost(id);
getComments(id);