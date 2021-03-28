function getChallenges(page) {
	fetch(`http://157.230.233.218:8080/api/challenges/feed?page=${page}`)
	.then(res => res.json()).then(function(json) {
		for(let i = 0; i < json["feed"].length; i++) {
			console.log(json["feed"]);
		}
	});
}