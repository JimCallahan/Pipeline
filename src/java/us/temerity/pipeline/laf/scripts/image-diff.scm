
(define (image-diff fileA nameA fileB nameB)
  (let* ((imgA (car (gimp-file-load 0 fileA fileA)))
	 (imgB (car (gimp-file-load 0 fileB fileB)))

	 (layersA (gimp-image-get-layers imgA))
	 (layerA (aref (cadr layersA) 0))

	 (layersB (gimp-image-get-layers imgB))
	 (layerB (aref (cadr layersB) 0))

	 (sizeX (car (gimp-drawable-width layerB)))
	 (sizeY (car (gimp-drawable-height layerB)))
	 (type (car (gimp-drawable-type layerB))) 
	 
	 (layerAB (car (gimp-layer-new imgA sizeX sizeY 1 nameB 100.0 0))))
  
    (gimp-image-add-layer imgA layerAB 1)
    
    (gimp-layer-set-name layerA nameA)
    (gimp-layer-set-mode layerA 6)

    (gimp-edit-copy layerB)
    (gimp-floating-sel-anchor (car (gimp-edit-paste layerAB 0)))

    (gimp-display-new imgA)
    )
  )
