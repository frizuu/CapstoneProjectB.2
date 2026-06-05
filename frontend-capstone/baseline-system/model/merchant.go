package model

import "time"

type Merchant struct {
	ID           int       
	Name         string    
	Balance      int64     
	MerchantCode string    
	Category     string    
	Status       string    
	CreatedAt    time.Time 
}
