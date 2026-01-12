# Manual Calculation for demo.csv

## Input Data
- **Flights:**
  - F1: DAC -> A (08:00-09:00, 1.0 hour, cost 100)
  - F2: A -> DAC (10:00-11:00, 1.0 hour, cost 100)
  - F3: DAC -> B (12:00-13:00, 1.0 hour, cost 100)
  - F4: B -> DAC (14:00-15:00, 1.0 hour, cost 100)

- **Base:** DAC
- **Constraints (defaults):**
  - Max duty time: 12 hours
  - Max flying time: 8 hours
  - Min turnaround: 40 minutes
  - Overnight: Not allowed

- **Cost Parameters (defaults):**
  - Fixed cost per duty: 200
  - Cost per flying hour: 100
  - Night penalty: 150 (not applicable - no night flights)
  - Overtime penalty: 120/hour (for duty > 8 hours)

## Valid Pairings Analysis

### Pairing 1: F1 alone (DAC -> A)
- **Flights:** F1
- **Route:** DAC -> A
- **Duty time:** 08:00 to 09:00 = 1.0 hour
- **Flying time:** 1.0 hour
- **Cost calculation:**
  - Flight cost: 100
  - Fixed cost: 200
  - Hourly cost: 100 × 1.0 = 100
  - Overtime: 0 (duty >= 8 hours)
  - **Total: 400**

### Pairing 2: F1-F2 (DAC -> A -> DAC)
- **Flights:** F1, F2
- **Route:** DAC -> A -> DAC
- **Connection check:** F1 arrives 09:00, F2 departs 10:00
  - Turnaround: 10:00 - 09:00 = 1 hour = 60 minutes >= 40
  - Location: DAC -> A -> DAC
- **Duty time:** 08:00 to 11:00 = 3.0 hours
- **Flying time:** 1.0 + 1.0 = 2.0 hours < 8 horus
- **Cost calculation:**
  - Flight costs: 100 + 100 = 200
  - Fixed cost: 200
  - Hourly cost: 100 × 2.0 = 200
  - Overtime: 0 (duty < 8 hours)
  - **Total: 600**

### Pairing 3: F3 alone (DAC -> B)
- **Flights:** F3
- **Route:** DAC -> B
- **Duty time:** 12:00 to 13:00 = 1.0 hour
- **Flying time:** 1.0 hour
- **Cost calculation:**
  - Flight cost: 100
  - Fixed cost: 200
  - Hourly cost: 100 × 1.0 = 100
  - **Total: 400**

### Pairing 4: F3-F4 (DAC -> B -> DAC)
- **Flights:** F3, F4
- **Route:** DAC -> B -> DAC
- **Connection check:** F3 arrives 13:00, F4 departs 14:00
  - Turnaround: 14:00 - 13:00 = 1 hour = 60 minutes >= 40 
  - Location: DAC -> B -> DAC
- **Duty time:** 12:00 to 15:00 = 3.0 hours
- **Flying time:** 1.0 + 1.0 = 2.0 hours < 8 hours
- **Cost calculation:**
  - Flight costs: 100 + 100 = 200
  - Fixed cost: 200
  - Hourly cost: 100 × 2.0 = 200
  - Overtime: 0 (duty <= 8 hours)
  - **Total: 600**

### Pairing 5: F1-F2-F3-F4 (DAC -> A -> DAC -> B -> DAC) OPTIMAL
- **Flights:** F1, F2, F3, F4
- **Route:** DAC -> A -> DAC -> B -> DAC
- **Connection checks:**
  - F1→F2: A -> A, turnaround 10:00 - 09:00 = 1 hour > 40 min 
  - F2→F3: DAC -> DAC, turnaround 12:00 - 11:00 = 1 hour > 40 min
  - F3→F4: B -> B, turnaround 14:00 - 13:00 = 1 hour > 40 min
- **Duty time:** 08:00 to 15:00 = 7.0 hours (< 12 hours) 
- **Flying time:** 1.0 + 1.0 + 1.0 + 1.0 = 4.0 hours (< 8 hours)
- **Cost calculation:**
  - Flight costs: 100 + 100 + 100 + 100 = 400
  - Fixed cost: 200
  - Hourly cost: 100 × 4.0 = 400
  - Overtime: 0 (duty = 7 hours < 8 hours)
  - **Total: 1000** 

### Note: F2 and F4 cannot START pairings
- F2 starts at A (not base DAC)
- F4 starts at B (not base DAC)
- The algorithm only creates pairings starting from the base

## Initial Solution (Individual Pairings)
In the initial solution, each flight is covered individually:
- F1 alone: 400
- F2 alone: 400 (but F2 doesn't start from base, so this pairing is invalid)
- F3 alone: 400
- F4 alone: 400 (but F4 doesn't start from base, so this pairing is invalid)

## Optimal Solution

The optimal solution should use connected pairings:

**Option A: Single long pairing**  OPTIMAL
- Pairing: F1-F2-F3-F4 (DAC -> A -> DAC -> B -> DAC) = 1000
- **Total Cost: 1000** 

**Option B: Two connected pairings**
- Pairing 1: F1-F2 (DAC -> A -> DAC) = 600
- Pairing 2: F3-F4 (DAC -> B -> DAC) = 600
- **Total Cost: 1200**

**Option C: Four individual pairings**
- F1 alone: 400
- F2 alone: 400
- F3 alone: 400
- F4 alone: 400
- **Total Cost: 1600**

**Option D: Mixed**
- F1-F2: 600
- F3 alone: 400
- F4 alone: 400
- **Total Cost: 1400**

**Option E: Mixed**
- F1 alone: 400
- F2 alone: 400
- F3-F4: 600
- **Total Cost: 1400**

## Optimal Solution (ACTUAL)
**Option A is optimal: 1000**
- Single pairing: F1-F2-F3-F4 (cost 1000)
- This is better than two separate pairings because:
  - Only ONE fixed cost (200) instead of two (400)
  - Same total flight costs (400)
  - Same total hourly costs (400)
  - **Savings: 200** (one less fixed cost)
