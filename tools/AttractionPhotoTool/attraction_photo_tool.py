import json, os, re, tkinter as tk
from tkinter import filedialog, messagebox, ttk
from pathlib import Path
from PIL import Image, ImageTk, ImageOps

WORLDS = {
"Wieliczka":["Rynek Górny","Zamek Żupny","Tężnia Solankowa","Szyb Daniłowicza","Komora Mikołaja Kopernika","Kaplica św. Kingi","Komora Weimar","Podziemne jezioro","Komora Staszica","Skarbnik"],
"Kraków":["Planty","Rynek Główny","Sukiennice","Kościół Mariacki","Brama Floriańska","Barbakan","Wawel","Smok Wawelski","Bulwary Wiślane","Kopiec Krakusa"],
"Tatry":["Kuźnice","Dolina Strążyska","Rusinowa Polana","Morskie Oko","Gęsia Szyja","Hala Gąsienicowa","Kasprowy Wierch","Giewont","Tatrzańska fauna","Dolina Pięciu Stawów"],
"Rzym":["Piazza Navona","Panteon","Fontanna di Trevi","Schody Hiszpańskie","Koloseum","Forum Romanum","Kapitol","Zamek Świętego Anioła","Villa Borghese","Circus Maximus"],
"Londyn":["Big Ben","Westminster","London Eye","Tower Bridge","Tower of London","British Museum","Hyde Park","Buckingham Palace","Natural History Museum","Katedra św. Pawła"],
"Mediolan":["Duomo","Galleria Vittorio Emanuele II","Zamek Sforzów","Brera","Navigli","La Scala","Muzeum Nauki i Techniki","Porta Nuova","San Siro","Arco della Pace"],
"Malta":["Valletta","Upper Barrakka Gardens","Trzy Miasta","Mdina","Rabat","Blue Grotto","Marsaxlokk","Gozo","Comino","Świątynie Ħaġar Qim"]}

def safe(s):
    import unicodedata
    s=unicodedata.normalize("NFKD",s).encode("ascii","ignore").decode().lower()
    return re.sub(r"[^a-z0-9]+","_",s).strip("_")

class App(tk.Tk):
    def __init__(self):
        super().__init__(); self.title("Kacper & Kapi — Zdjęcia atrakcji"); self.geometry("1180x760"); self.minsize(980,650)
        self.src=None; self.tkimg=None; self.zoom=1.0; self.ox=0; self.oy=0
        self.out=Path.home()/"KacperKapi_Attractions"; self.out.mkdir(exist_ok=True)
        self.build(); self.world.current(0); self.fill_places(); self.place.current(0); self.refresh_info()

    def build(self):
        left=ttk.Frame(self,padding=14); left.pack(side="left",fill="y")
        ttk.Label(left,text="Kacper & Kapi",font=("Segoe UI",18,"bold")).pack(anchor="w")
        ttk.Label(left,text="Przygotowanie zdjęć atrakcji 16:9").pack(anchor="w",pady=(0,18))
        ttk.Label(left,text="Świat").pack(anchor="w"); self.world=ttk.Combobox(left,state="readonly",values=list(WORLDS),width=28); self.world.pack(fill="x"); self.world.bind("<<ComboboxSelected>>",lambda e:self.fill_places())
        ttk.Label(left,text="Miejsce").pack(anchor="w",pady=(12,0)); self.place=ttk.Combobox(left,state="readonly",width=28); self.place.pack(fill="x"); self.place.bind("<<ComboboxSelected>>",lambda e:self.refresh_info())
        self.info=ttk.Label(left,text="",wraplength=260); self.info.pack(anchor="w",pady=14)
        ttk.Button(left,text="1. Wybierz zdjęcie…",command=self.open_image).pack(fill="x",pady=4)
        ttk.Label(left,text="Powiększenie").pack(anchor="w",pady=(15,0)); self.scale=ttk.Scale(left,from_=1,to=3,command=self.zoom_change); self.scale.set(1); self.scale.pack(fill="x")
        ttk.Label(left,text="Przesuwanie kadru: przeciągnij zdjęcie myszką.").pack(anchor="w",pady=8)
        ttk.Button(left,text="2. Zapisz gotowe 16:9",command=self.save).pack(fill="x",pady=8)
        ttk.Button(left,text="Otwórz folder wynikowy",command=lambda:os.startfile(self.out)).pack(fill="x")
        ttk.Separator(left).pack(fill="x",pady=16)
        self.done=ttk.Label(left,text=""); self.done.pack(anchor="w")
        main=ttk.Frame(self,padding=14); main.pack(side="left",fill="both",expand=True)
        ttk.Label(main,text="Podgląd dokładnego kadru w grze (16:9)",font=("Segoe UI",14,"bold")).pack(anchor="w")
        self.canvas=tk.Canvas(main,bg="#202020",highlightthickness=0); self.canvas.pack(fill="both",expand=True,pady=10)
        self.canvas.bind("<Configure>",lambda e:self.draw()); self.canvas.bind("<ButtonPress-1>",self.drag_start); self.canvas.bind("<B1-Motion>",self.drag)
        ttk.Label(main,text="Program nie rozciąga zdjęcia. Nadmiar jest przycinany, a zapis ma 1280×720 px.").pack(anchor="w")

    def fill_places(self):
        self.place["values"]=WORLDS[self.world.get()]; self.place.current(0); self.src=None; self.refresh_info(); self.draw()
    def refresh_info(self):
        wi=list(WORLDS).index(self.world.get())+1; si=self.place.current()+1
        name=f"w{wi}s{si}_{safe(self.place.get())}.jpg"; self.info.config(text=f"ID: w{wi}s{si}\nPlik wynikowy: {name}")
        count=sum(1 for p in self.out.glob("*.jpg")); self.done.config(text=f"Gotowe zdjęcia: {count}/70")
    def open_image(self):
        p=filedialog.askopenfilename(filetypes=[("Zdjęcia","*.jpg *.jpeg *.png *.webp"),("Wszystkie pliki","*.*")])
        if not p:return
        self.src=Image.open(p).convert("RGB"); self.zoom=1; self.ox=self.oy=0; self.scale.set(1); self.draw()
    def zoom_change(self,v): self.zoom=float(v); self.draw()
    def viewport(self):
        w=max(100,self.canvas.winfo_width()); h=max(100,self.canvas.winfo_height()); vw=w-30; vh=min(h-30,int(vw*9/16)); vw=int(vh*16/9); return (w-vw)//2,(h-vh)//2,vw,vh
    def render(self,w,h):
        if not self.src:return None
        iw,ih=self.src.size; base=max(w/iw,h/ih)*self.zoom; nw,nh=int(iw*base),int(ih*base)
        im=self.src.resize((nw,nh),Image.Resampling.LANCZOS)
        maxx=max(0,(nw-w)//2); maxy=max(0,(nh-h)//2); cx=nw//2+int(self.ox); cy=nh//2+int(self.oy); cx=max(w//2,min(nw-w//2,cx)); cy=max(h//2,min(nh-h//2,cy))
        return im.crop((cx-w//2,cy-h//2,cx-w//2+w,cy-h//2+h))
    def draw(self):
        self.canvas.delete("all"); x,y,w,h=self.viewport(); self.canvas.create_rectangle(x,y,x+w,y+h,outline="white",width=2)
        im=self.render(w,h)
        if im:
            self.tkimg=ImageTk.PhotoImage(im); self.canvas.create_image(x,y,anchor="nw",image=self.tkimg)
        else:self.canvas.create_text(x+w/2,y+h/2,text="Wybierz zdjęcie atrakcji",fill="white",font=("Segoe UI",18))
    def drag_start(self,e): self.dx=e.x; self.dy=e.y
    def drag(self,e): self.ox+=e.x-self.dx; self.oy+=e.y-self.dy; self.dx=e.x; self.dy=e.y; self.draw()
    def save(self):
        if not self.src:return messagebox.showwarning("Brak zdjęcia","Najpierw wybierz zdjęcie.")
        im=self.render(1280,720); wi=list(WORLDS).index(self.world.get())+1; si=self.place.current()+1
        name=f"w{wi}s{si}_{safe(self.place.get())}.jpg"; im.save(self.out/name,"JPEG",quality=92,optimize=True)
        self.refresh_info(); messagebox.showinfo("Gotowe",f"Zapisano:\n{self.out/name}")

if __name__=="__main__": App().mainloop()
