
(define (build-mipmap in outfix maxsize)
  (let* ((img (car (gimp-file-load 1 in in)))
	 (size (* maxsize 2)))
    (while (> size 1)
	   (set! size (/ size 2))
	   (gimp-image-scale img size size)
	   (let* ((layers (gimp-image-get-layers img))
		  (layer (aref (cadr layers) 0))
		  (file (string-append outfix "." (number->string size) ".png")))
	     (print (string-append "Writing: " file))
	     (gimp-file-save 1 img layer file file)
	     )
	   )
    (gimp-image-delete img)
    )
  )

