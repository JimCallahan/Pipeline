
(define (web-icon infile outfile)
  (let* ((img (car (gimp-file-load 1 infile infile))))

    (gimp-image-scale img 64 64)

    (gimp-palette-set-background '(128 128 128)) 
    (gimp-layer-new img 64 64 0 "background" 100.0 0)
    (gimp-image-flatten img)

    (gimp-convert-indexed img 1 0 255 0 0 "")

    (let* ((layers (gimp-image-get-layers img))
	   (layer (aref (cadr layers) 0)))
      (print (string-append "Writing: " outfile))
      (gimp-file-save 1 img layer outfile outfile)
      )

    (gimp-image-delete img)
    )
  )
