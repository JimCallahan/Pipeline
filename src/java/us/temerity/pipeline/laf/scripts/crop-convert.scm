
(define (crop-convert infile outfile ox oy sx sy)
  (let* ((img (car (gimp-file-load 1 infile infile))))

    (gimp-crop img sx sy ox oy)

    (let* ((layers (gimp-image-get-layers img))
	   (layer (aref (cadr layers) 0)))
      (print (string-append "Writing: " outfile))
      (gimp-file-save 1 img layer outfile outfile)
      )

    (gimp-image-delete img)
    )
  )
