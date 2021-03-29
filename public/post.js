function getUrlVars() {
	var vars = {};
	var parts = window.location.href.replace(/[?&]+([^=&]+)=([^&]*)/gi, function (m, key, value) {
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
		.then(function (json) {
			displayPost(json);
		})
		.catch(function (error) {
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

	for (let i = 0; i < comments.length; i++) {
		if (comments[i]["reply_to_id"] != null) {
			if (comments[i]["reply_to_id"]["$oid"] == comment["_id"]["$oid"]) {
				returnComments.push(comments[i]);
				let temp = getChildComments(comments[i]);
				if (temp.length > 0) {
					returnComments.push(temp);
				}
			}
		}
	}

	return returnComments;
}

function displayComments() {
	let commentsDisplay = [];

	for (let i = 0; i < comments.length;) {
		if (comments[i]["reply_to_id"] == null) {
			let temp = getChildComments(comments[i]);
			commentsDisplay.push(comments[i]);
			commentsDisplay.push(temp);
		}
		comments.shift()

	}


	formatReplies(commentsDisplay);
	document.getElementById("loadingComments").remove();
}

/**
 * Sets the post fields to display a post
 * @param vals json of post data
 */
function displayPost(vals) {
	document.getElementById("title").innerHTML = vals.title;
	document.getElementById("author").innerHTML = vals.author;
	document.getElementById("author").href = "/user.html?" + vals.author;
	document.getElementById("date").innerHTML = new Date(vals.date.$date).toLocaleString();
	document.getElementById("body").innerHTML = formatText(vals.body);

	document.getElementById("loadingPost").remove();
}


let num = -2;
let display = "";


/**
 * Displays all comments on post, formatted correctly 
 * @param replyArr Array of comments, with replies (EX. [CommentA, [CommentB, [], CommentB, [CommentC, []]]])
 */
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

/** Displays a comment in the commentDisplayField 
 * @param reply text to display
 * @param number indent to offset by
 * @param user Username of the author
 * @param cid Id of the comment, used for replies
*/
function load(reply, number, user, cid) {
	date = "DATE HERE"
	let comment = `<div class="commentDisplay" name="${number}" id="${cid}" style="left: ${(30 * number) + 30}px; position: relative;" > <div style="display: flex;"> <a href="/user.html?${user}"> ${user} </a> <p style="width: 30%;position: relative;padding: 0 0 0 30px;margin: 0 0 0 0;"> ${date} </p> </div> <p> ${formatText(reply)} </p> <div id="options"> <button onClick="openCommentField(this, '${cid}')" style="left: 25px;position: relative;"> Reply </button> </div> </div> `

	let shell = document.getElementById("comments");

	shell.innerHTML += comment;
}

/**
 * Opens the reply to comment/post field
 * @param el Button that opens reply field
 * @param cid Id of the comment
 */
function openCommentField(el, cid) {
	if (cid == null && el.parentElement.parentElement.childElementCount < 2) {
		//REPLYING TO POST
		let commentField = `<div class="commentInputField"> <input placeholder="Comment" id="inputField${cid}"> <div id="commentTooLong" class="error">Your comment is over 1500 characters long</div><br> <button onClick="makeCommentOnPost(this.parentElement.childNodes[1].value); this.parentElement.remove()"> Submit </button> <button onClick="this.parentElement.remove()"> Cancel </button> </div>`
		el.parentElement.parentElement.innerHTML += commentField;

		document.getElementById(`inputField${cid}`).focus();
	} else if (cid != null && el.parentElement.parentElement.childElementCount < 4) {
		//REPLYING TO COMMENT
		let commentField = `<div class="commentInputField"> <input placeholder="Comment" id="inputField${cid}"> <div id="commentTooLong${cid}" class="error">Your comment is over 1500 characters long</div><br> <button onClick="makeComment('${cid}', this.parentElement.childNodes[1].value); this.parentElement.remove()"> Submit </button> <button onClick="this.parentElement.remove()"> Cancel </button> </div>`
		el.parentElement.parentElement.innerHTML += commentField;

		document.getElementById(`inputField${cid}`).focus();
	}
}

function makeComment(commentId, body) {
	if (body.length <= 1500) {
		document.getElementById(`commentTooLong${commentId}`).style.display = "none";
		let fetchUrl = "http://157.230.233.218:8080/api/comments/";
		fetch(fetchUrl, {
			method: "POST",
			body: JSON.stringify({ "reply_to_id": { "$oid": commentId }, "body": body, "post_id": id }),
			mode: "cors",
			headers: {
				"Content-type": "application/json; charset=UTF-8",
				"Authorization": getCookie("token"),
				"Origin": "http://healthfirst4342.tk/"
			}
		});
	} else {
		document.getElementById(`commentTooLong${commentId}`).style.display = "block";
	}
}

function makeCommentOnPost(body) {
	if (body.length <= 1500) {
		document.getElementById("commentTooLong").style.display = "none";
		let fetchUrl = "http://157.230.233.218:8080/api/comments/";
		fetch(fetchUrl, {
			method: "POST",
			body: JSON.stringify({ "body": body, "post_id": id }),
			mode: "cors",
			headers: {
				"Content-type": "application/json; charset=UTF-8",
				"Authorization": getCookie("token"),
				"Origin": "http://healthfirst4342.tk/"
			}
		});
	} else {
		document.getElementById("commentTooLong").style.display = "block";
	}
}

function formatText(text) {
	fetch('https://api.github.com/markdown', {method:"POST", body: JSON.stringify({"text": text}) } ).then(res => res.text()).then(function(json) {return (json)})

	// text = text.split("\n").join("<br>");
	// text = text.split(" ");
	// text = text.join("&nbsp;")
	// text = text.split("**");

	// for (let i = 0; i < text.length; i++) {
	// 	if (i % 2 != 0) {
	// 		text[i] = "<b>" + text[i] + "</b>"
	// 	}
	// }

	// text = text.join("").split("*");

	// for (let i = 0; i < text.length; i++) {
	// 	if (i % 2 != 0) {
	// 		text[i] = "<i>" + text[i] + "</i>"
	// 	}
	// }

	// return text.join("");
}

getPost(id);
getComments(id);