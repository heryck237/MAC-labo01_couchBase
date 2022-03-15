## 2 Le titre des films parfaitement notés (10) par les critiques tomatoes mais qui n’ont pas été notés par les viewers tomatoes

SELECT M.title
FROM `mflix-sample`.\_default.movies M
WHERE M.tomatoes.critic.rating = 10 AND M.tomatoes.viewer.rating IS MISSING;

Results

[
{
"title": "Short Term 12"
}
]

## 4 Le nom des personnes ayant fait plus de 300 commentaires. Le résultat de la requête doit être une liste de String

SELECT C.name
FROM `mflix-sample`.\_default.comments C
GROUP BY C.name
HAVING COUNT(\*) > 300;

---

SELECT C.name, COUNT(_) nb_comments
FROM `mflix-sample`.\_default.comments C
GROUP BY C.name
HAVING COUNT(\*) > 300;

Results
name nb_comments
"Brienne of Tarth" 302
"Sansa Stark" 308
"Thoros of Myr" 304
"Missandei" 327
"Mace Tyrell" 331
"The High Sparrow" 315
"Rodrik Cassel" 305
"Robert Jordan" 304

## 6 Le nom et le nombre de films des directeurs ( director_name , count_film ) ayant dirigé plus que 30 films.

with all_director AS (SELECT DISTINCT directors as name, count(*) nb_film
FROM `mflix-sample`._default.movies
UNNEST directors
GROUP BY directors)

SELECT all_director.name director_name, all_director.nb_film count_film
FROM all_director
WHERE all_director.nb_film > 30;

## 8 2manières différentes pour retourner le text (doit être de type string et non pas de type array) de tous les commentaires sur tous les films ( movie_id ) dirigés par un directeur donné (par exemple "Woody Allen").
- première
  SELECT M._id AS movie_id, C.text
  FROM `mflix-sample`._default.movies M
  INNER JOIN `mflix-sample`._default.comments C
  ON M._id = C.movie_id
  WHERE "Woody Allen" WITHIN M.directors;

- deuxieme
  SELECT M._id movies_id, C.text
  FROM `mflix-sample`._default.movies M
  INNER JOIN `mflix-sample`._default.comments C
  ON M._id = C.movie_id AND "Woody Allen" WITHIN M.directors;
## 10 les films qui sont projetés uniquement à partir de 18h.

SELECT DISTINCT M.title
FROM `mflix-sample`._default.movies M
WHERE M._id NOT IN (
	SELECT DISTINCT T.movieId
	FROM `mflix-sample`._default.theaters T
	WHERE T.hourBegin < '18:00:00'
);

with early_projection AS (select distinct sched.movieId movies_id, sched.hourBegin hour
FROM `mflix-sample`._default.theaters
UNNEST schedule as sched
WHERE sched.hourBegin < '18:00:00')

SELECT M._id, M.title
from `mflix-sample`._default.movies M
Where M._id NOT IN (select early_projection.movies_id);